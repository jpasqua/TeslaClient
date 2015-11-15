/*
 * Tesla.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 5, 2013
 */

package org.noroomattheinn.tesla;

import java.io.IOException;
import java.io.StringWriter;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.lang3.StringUtils;
import org.noroomattheinn.utils.Pair;
import org.noroomattheinn.utils.RestHelper;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.json.JSONWriter;
import us.monoid.web.Content;
import us.monoid.web.JSONResource;
import us.monoid.web.Resty;

/**
 * Tesla: This class represents a connection to Tesla's servers and provides
 * access to Vehicle objects.
 * <P>
 * The basic pattern of use is:<ol>
 * <li>Create a Tesla object
 * <li>Connect to the Tesla portal by passing login credentials to the
 * connect() method
 * <li>Get a list of Vehicles associated with the account
 * <li>Query and control a vehicle using the Vehicle object
 * </ol>
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public class Tesla {
/*------------------------------------------------------------------------------
 *
 * Constants and Enums
 * 
 *----------------------------------------------------------------------------*/
    
    public  static final Logger logger = Logger.getLogger(Tesla.class.getName());
    
    private static final String apiName = "Tesla Client API";
    private static final String TeslaURI = "https://owner-api.teslamotors.com/";
    private static final String APIVersion = "api/1/";
    
    private static final RestHelper.Throttle Throttle;
    static {
        List<Pair<Integer,Integer>> rateLimits = new ArrayList<>();
        rateLimits.add(new Pair<>(10, 10));     // No more than 10 requests in 10 seconds
        rateLimits.add(new Pair<>(20, 60));     // No more than 20 requests/minute
        rateLimits.add(new Pair<>(150, 10*60)); // No more than 150 requests/(10 minutes)
        Throttle = new RestHelper.Throttle(rateLimits);
    }
    private static final String TeslaUserAgent =
            "Model S 2.1.79 (Nexus 5; Android REL 4.4.4; en_US)";
    private static final RestHelper.UAOption UserAgent =
            new RestHelper.UAOption(TeslaUserAgent);
    
/*------------------------------------------------------------------------------
 *
 * Internal State
 * 
 *----------------------------------------------------------------------------*/
        
    private final Resty api;
    private List<Vehicle> vehicles;
    private String username;
    private String token;
    
/*==============================================================================
 * -------                                                               -------
 * -------              Public Interface To This Class                   ------- 
 * -------                                                               -------
 *============================================================================*/
    
    public Tesla() {
        api = createConnection(60* 1000);
        vehicles = new ArrayList<>();
    }
        
    final Resty createConnection(int readTimeout) {
        return RestHelper.getInstance(
                new RestHelper.ReadTimeout(readTimeout),
                UserAgent, Throttle);
    }
    
/*------------------------------------------------------------------------------
 *
 * Methods for connecting to and authenticating with Tesla's server
 * 
 *----------------------------------------------------------------------------*/
    /*
     * Try connecting with the supplied token.
     * If we can login, go ahead and fetch the vehicle list while we're at it.
     * @return  true    The connection based on stored cookies succeeded
     *          false   No dice, the user must supply credentials
     */
    public boolean connectWithToken(String username, String token) {
        api.withHeader("Authorization", "Bearer " + token);
        vehicles = queryVehicles();
        if (!vehicles.isEmpty()) {
            this.token = token;
            this.username = username;
            return true;
        } else {
            this.token = null;
            this.username = null;
            return false;
        }
    }

    /*
     * Try connecting with the supplied credentials. If the login succeeds, the
     * credentials and session info will be stored in cookies for the future.
     * If we can login, go ahead and fetch the vehicle list while we're at it.
     * @return  true    The connection succeeded
     *          false   No dice, couldn't connect or fetch vehicles
     */
    public boolean connect(String username, String password) {
        String[] apiMaterial = getAPIMaterial();

        // Create the payload
        String payload;
        try
        {
           StringWriter stringWriter = new StringWriter();
           JSONWriter writer = new JSONWriter( stringWriter );
           writer.object()
              .key( "grant_type" )   .value( "password" )
              .key( "client_id" )    .value( apiMaterial[0] )
              .key( "client_secret" ).value( apiMaterial[1] )
              .key( "email" )        .value( username )
              .key( "password" )     .value( password )
              .endObject();

           payload = stringWriter.toString();
        }
        catch( JSONException ex ) {
           throw new Error( "Big problem. Can't write to string.", ex );
        }

        try {
            JSONResource r = api.json(
                    rawEndpoint("oauth/token"),
                    Resty.content(new JSONObject(payload)));
            if (r == null) return false;
            String accessToken = r.object().getString("access_token");
            if (accessToken == null) return false;
            return connectWithToken(username, accessToken);
        } catch (IOException | JSONException e) {
            logger.warning("Trouble connecting: " + e.getMessage());
            return false;
        }
    }
    
    public String getUsername() { return username; }
    public String getToken() { return token; }
    
/*------------------------------------------------------------------------------
 *
 * Methods access data about vehicles
 * 
 *----------------------------------------------------------------------------*/
    
    public List<Vehicle> queryVehicles() {
        List<Vehicle> list = new ArrayList<>(2);
        try {
            JSONResource r = api.json(apiEndpoint("vehicles"));
            JSONArray rawVehicleData = r.object().getJSONArray("response");
            int numVehicles = rawVehicleData.length();
            for (int i = 0; i < numVehicles; i++) {
                Vehicle vehicle = new Vehicle(this, rawVehicleData.getJSONObject(i));
                list.add(vehicle);
            }
        } catch (IOException | JSONException ex) {
            logger.warning("Problem fetching vehicle list: " + ex);
        }
        return list;
    }

    public List<Vehicle> getVehicles() { return vehicles; }


/*------------------------------------------------------------------------------
 *
 * Package Methods use to access the Tesla REST API
 * 
 *----------------------------------------------------------------------------*/
    
    String rawEndpoint(String name) {
        return TeslaURI + name;
    }

    String apiEndpoint(String name) {
        return rawEndpoint(APIVersion + name);
    }

    String vehicleSpecific(String vid, String name) {
        return apiEndpoint("vehicles/" + vid + "/" + name);
    }

    String vehicleCommand(String vid, String name) {
        return vehicleSpecific(vid, "command/" + name);
    }
    
    String vehicleData(String vid, String name) {
        return vehicleSpecific(vid, "data_request/" + name);
    }

    JSONObject getState(String state) { return call(state, null); }
    
    JSONObject invokeCommand(String command) { return invokeCommand(command, "{}"); }
        
    JSONObject invokeCommand(String command, String payload) {
        Content c;
        try {
            c = Resty.content(new JSONObject(payload));
        } catch (JSONException ex) {
            Tesla.logger.severe("Can't Happen - JSON Syntax Error: " + payload);
            return new JSONObject();
        }
        return call(command, c);
    }
    
    private JSONObject call(String command, Content payload) {
        JSONObject rawResponse = null;
        try {
            if (payload == null) rawResponse = api.json(command).object();
            else rawResponse = api.json(command, payload).object();
            if (rawResponse == null) return new JSONObject();
            return rawResponse.getJSONObject("response");
        } catch (IOException | JSONException ex) {
            String error = ex.toString().replace("\n", " -- ");
            Tesla.logger.finer(
                    "Failed invoking (" + 
                    StringUtils.substringAfterLast(command, "/") + "): [" + 
                    StringUtils.substringAfter(error, "[") );
            return (rawResponse == null) ? new JSONObject() : rawResponse;
        }
    }

    
/*------------------------------------------------------------------------------
 *
 * Private Utility Methods
 * 
 *----------------------------------------------------------------------------*/
    
    private static final byte[] ci = {
         115,  -51,   67, -104, -107,   16, -116, -114,
         -11, -120,   41,   84, -106,  -15,  -67,   78,
         -10,  -24,  -47,  124,   35,   73,   10,   43,
          -9,  123,  127,  126, -114,   58,   23,    3,
         115,  -70, -115,   46,   17,   87, -115,   31, 
         -67,  -90, -107, -100,   59,   18,  -19,   91,
          95,  -52,   82,   91,  -37,  -83,  -74,   39,
          12,   59,   14,  -81,    3,   95, -111,   72};
    private static final byte[] cs = {
         -28,   97,  -94,  108,   69,  -40,  111,   53,
          88,  -57,   82,  111,   57,   98,  116,  -63,
         -75,  -37,   16,   95,    2, -113,  -46, -112,
          32,   73,  -43,   23, -114,   38, -110,  -85,
         -42,   41,   98,  118,   30,   -2,  -11,   93,
          22,   89,   56,  105, -128,   20,  -24, -108,
          76,   31,  -19,   60,   69,  -98, -122,   54,
          67,   19,   72,  -37,  106,   62, -120,  -52};
    
    private String[] getAPIMaterial() {
        try {
            SecretKeySpec key = new SecretKeySpec(apiName.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
            String[] m = new String[2];
            byte[] plainText = new byte[ci.length];
            cipher.init(Cipher.DECRYPT_MODE, key);
            int ptLength = cipher.update(ci, 0, ci.length, plainText, 0);
            cipher.doFinal(plainText, ptLength);
            m[0] = new String(plainText);
            
            cipher.init(Cipher.DECRYPT_MODE, key);
            ptLength = cipher.update(cs, 0, cs.length, plainText, 0);
            cipher.doFinal(plainText, ptLength);
            m[1] = new String(plainText);
            return m;
        } catch (NoSuchAlgorithmException | InvalidKeyException |
                ShortBufferException | NoSuchPaddingException |
                IllegalBlockSizeException | BadPaddingException e) {
            Tesla.logger.severe("Could not decrypt APIMaterial");
            throw new Error("Logic Error: Can't decrypt APIMaterial");
        }
    }
}
