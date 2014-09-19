/*
 * Tesla.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 5, 2013
 */

package org.noroomattheinn.tesla;

import java.io.IOException;
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
import org.noroomattheinn.utils.RestyWrapper;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.web.JSONResource;
import us.monoid.web.Resty;

/**
 * Tesla: This class is the starting point for communicating with Tesla's
 * portal for querying and controlling Tesla vehicles.
 * <P>
 * The basic sequence of events is:<ol>
 * <li>Create a Tesla object
 * <li>Connect to the Tesla portal by passing login credentials to the
 * connect() method
 * <li>Get a list of Vehicles associated with the account
 * <li>Query and control a vehicle using the State and Controller objects by
 * supplying a Vehicle object
 * </ol>
 * We use a persistent cookie store to maintain login credentials and sessions.
 * We also use the cookie store to persist other local information like
 * the username. We need to keep that around even if we have auth cookies because
 * the Streaming API needs it. Rather than ask the user for it again, we keep
 * it from the first time she logs in using it.
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public class Tesla {
    // Constants
    private static final String apiName = "Tesla Client API";
    private static final String TeslaURI = "https://owner-api.teslamotors.com/";
    private static final String APIVersion = "api/1/";
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
    
    // Class Variables
    public static final Logger logger = Logger.getLogger(Tesla.class.getName());
    
    // Instance Variables
    private final RestyWrapper api;
    private final List<Vehicle> vehicles;
    private String username;
    private String token;
    
    //
    // Constructors
    //
    
    public Tesla() {
        this(null, -1);
    }
    
    public Tesla(String proxyHost, int proxyPort) {
        RestyWrapper.setProxy(proxyHost, proxyPort);
        api = new RestyWrapper(60*1000);    // Never wait more than a minute
        vehicles = new ArrayList<>();
    }
    
    //
    // Methods to build endpoint strings
    //
    
    public static String rawEndpoint(String name) {
        return TeslaURI + name;
    }

    public static String apiEndpoint(String name) {
        return rawEndpoint(APIVersion + name);
    }

    public static String vehicleSpecific(String vid, String name) {
        return apiEndpoint("vehicles/" + vid + "/" + name);
    }

    public static String vehicleCommand(String vid, String name) {
        return vehicleSpecific(vid, "command/" + name);
    }
    
    public static String vehicleData(String vid, String name) {
        return vehicleSpecific(vid, "data_request/" + name);
    }
    
    public boolean isCarAwake(Vehicle v) {
        vehicles.clear();
        if (fetchVehiclesInto(vehicles)) {
            for (Vehicle car : vehicles) {
                if (car.getVIN().equals(v.getVIN())) {
                    return (!car.status().equals("asleep"));
                }
            }
        }
        return false;
    }
    
    //
    // login / connect Methods
    // 
    
    /*
     * Try connecting with the supplied token.
     * If we can login, go ahead and fetch the vehicle list while we're at it.
     * @return  true    The connection based on stored cookies succeeded
     *          false   No dice, the user must supply credentials
     */
    public boolean connectWithToken(String username, String token) {
        vehicles.clear();
        api.withHeader("Authorization", "Bearer " + token);
        if (fetchVehiclesInto(vehicles)) {
            this.token = token;
            this.username = username;
            return true;
        }
        this.token = null;
        this.username = null;
        return false;
    }

    private static final String LoginPayloadFormat = "{" + 
        " \"grant_type\" : \"password\", " + 
        " \"client_id\" : \"%s\", " +
        " \"client_secret\" : \"%s\", " +
        " \"email\" : \"%s\", " + 
        " \"password\" : \"%s\" }";
    
    /*
     * Try connecting with the supplied credentials. If the login succeeds, the
     * credentials and session info will be stored in cookies for the future.
     * If we can login, go ahead and fetch the vehicle list while we're at it.
     * @return  true    The connection succeeded
     *          false   No dice, couldn't connect or fetch vehicles
     */
    public boolean connect(String username, String password) {
        String[] apiMaterial = getAPIMaterial();
        String payload = String.format(
                LoginPayloadFormat, apiMaterial[0], apiMaterial[1], username, password);
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

    //
    // Vehicle handling Methods
    //
    
    boolean fetchVehiclesInto(List<Vehicle> list) {
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
            return false;
        }
        return true;
    }

    public String getUsername() { return username; }
    public String getToken() { return token; }
    
    public List<Vehicle> getVehicles() { return vehicles; }

    public void setProxy(String proxyHost, int proxyPort) {
        RestyWrapper.setProxy(proxyHost, proxyPort);
    }
    
    //
    // Field Accessor Methods
    //
    
    public RestyWrapper getAPI() { return api; }
    
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
