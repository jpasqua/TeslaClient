/*
 * SnapshotState.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 11, 2013
 */

package org.noroomattheinn.tesla;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.noroomattheinn.utils.RestyWrapper;
import org.noroomattheinn.utils.Utils;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.web.TextResource;

/**
 * StreamingState: Provides access to streaming information about the current
 * state of the vehicle.
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public class SnapshotState extends APICall {
    
/*------------------------------------------------------------------------------
 *
 * Constants and Enums
 * 
 *----------------------------------------------------------------------------*/
    private enum Keys {
        timestamp, odometer, speed, soc, elevation, est_heading,
        est_lat, est_lng, power, shift_state, range};
    private final Keys[] keyList = Keys.values();
    private final String allKeys =
            StringUtils.join(keyList, ',', 1, keyList.length);

    private static final String endpointFormat = 
            "https://streaming.vn.teslamotors.com/stream/%s/?values=%s";

    private static final int WakeupRetries = 3;
    private static final int ReadTimeoutInMillis = 1 * 1000;
    
/*------------------------------------------------------------------------------
 *
 * Internal State
 * 
 *----------------------------------------------------------------------------*/
    
    private Vehicle vehicleWithToken = null;
    private BufferedReader reader = null;
    
    
/*==============================================================================
 * -------                                                               -------
 * -------              Public Interface To This Class                   ------- 
 * -------                                                               -------
 *============================================================================*/
    
    // Accessors
    public Date   timestamp() {return(new Date(getLong(Keys.timestamp))); }
    public double speed() { return(getDouble(Keys.speed)); }
    public double odometer() {return(getDouble(Keys.odometer)); }
    public int    soc() { return(getInteger(Keys.soc)); }
    public int    elevation() { return(getInteger(Keys.elevation)); }
    public int    estHeading() { return(getInteger(Keys.est_heading));}
    public double estLat() { return(getDouble(Keys.est_lat)); }
    public double estLng() { return(getDouble(Keys.est_lng)); }
    public int    power() { return(getInteger(Keys.power)); }
    public String shiftState() { return(getString(Keys.shift_state)); }
    public int    range() { return(getInteger(Keys.range)); }
    @Override public String getStateName() { return "Unstreamed State"; }

    
    // Constructors
    public SnapshotState(Vehicle v) {
        super(v);
    }
    

/*------------------------------------------------------------------------------
 *
 * Methods overridden from APICall
 * 
 *----------------------------------------------------------------------------*/
    
    @Override public boolean refresh() {
        reader = null;
        for (int i = 0; i < 2; i++) {
            prepare();
            refreshStream();
            if (hasValidData())
                return true;
            reader = null;
            Utils.sleep(1000);
        }
        return false;
    }
    
    public boolean refreshStream () {
        JSONObject val = produce(reader);
        if (val == null) {
            invalidate();
            return false;
        }

        setState(val);
        return true;
    }
    
    
/*------------------------------------------------------------------------------
 *
 * Methods overridden from Object
 * 
 *----------------------------------------------------------------------------*/
    
    
    @Override public String toString() {
        return String.format(
                "Time Stamp: %s.%s\n" +
                "Speed: %3.1f\n" +
                "Location: [(Lat: %f, Lng: %3f), Heading: %d, Elevation: %d]\n" +
                "Charge Info: [SoC: %d, Power: %d]\n" +
                "Odometer: %7.1f\n" +
                "Range: %d\n",
                timestamp().getTime()/1000, timestamp().getTime()%1000,
                speed(),
                estLat(), estLng(), estHeading(), elevation(),
                soc(), power(),
                odometer(), range()
                // Don't know what shift_state is! Always seems to be null
                );
    }
    
/*------------------------------------------------------------------------------
 *
 * Methods for setting up the Streaming connection and reading the data
 * 
 *----------------------------------------------------------------------------*/
    
    private JSONObject produce(BufferedReader reader) { 
        if (reader == null) return null;
        try {
            String line = reader.readLine();
            if (line == null) return null;

            JSONObject jo = new JSONObject();
            String vals[] = line.split(",");
            for (int i = 0; i < keyList.length; i++) {
                try {
                    jo.put(keyList[i], vals[i]);
                } catch (JSONException ex) {
                    Tesla.logger.log(Level.SEVERE, "Malformed data", ex);
                    return null;
                }
            }
            return jo;
        } catch (IOException ex) {
            Tesla.logger.log(Level.FINEST, "Timeouts are expected here...", ex);
            return null;
        }
    }

    private void prepare() {
        prepareInternal();
        if (reader != null) return;
        Utils.sleep(1000);
        prepareInternal();  // Try again. Auth tokens may have expired.
    }

        
    private void prepareInternal() {
        if (reader != null) return;
            
        if (vehicleWithToken == null) {
            vehicleWithToken = getVehicleWithAuthToken(v);
            if (vehicleWithToken == null)  return;
        }

        String endpoint = String.format(
                endpointFormat, vehicleWithToken.getStreamingVID(), allKeys);

        try {
            TextResource r = getAuthAPI(vehicleWithToken).text(endpoint);
            reader = new BufferedReader(new InputStreamReader(r.stream()));
        } catch (IOException ex) {
            // Timed out or other problem
            Tesla.logger.log(Level.INFO, "Auth tokens may have expired, will retry", ex.getMessage());
            vehicleWithToken = null;    // Tokens may have expired, force refetch
            reader = null;
        }
    }
    
/*------------------------------------------------------------------------------
 *
 * This section contains the methods and classes necessary to get auth
 * tokens and create an authenticated connection to Tesla
 * 
 *----------------------------------------------------------------------------*/
        
    private void setAuthHeader(RestyWrapper api, String username, String authToken) {
        byte[] authString = (username + ":" + authToken).getBytes();
        String encodedString = Base64.encodeBase64String(authString);
        api.withHeader("Authorization", "Basic " + encodedString);
    }


    private Vehicle getVehicleWithAuthToken(Vehicle basedOn) {
        String vid = basedOn.getVID();
        // Remember this so we can find the right vehicle when we fetch
        // the updated list of vehicles after doing the wakeup

        ActionController a = new ActionController(basedOn);
        for (int i = 0; i < WakeupRetries; i++) {

            List<Vehicle> vList = new ArrayList<>();
            basedOn.getContext().fetchVehiclesInto(vList);
            for (Vehicle newV : vList) {
                if (newV.getVID().equals(vid) && newV.getStreamingToken() != null) {
                    return newV;
                }
            }
            a.wakeUp(); Utils.sleep(500);
        }

        // For some reason we can't get Streaming tokens. We've tried enough
        // so give up 
        Tesla.logger.log(Level.WARNING, "Error: couldn't retreive auth tokens");
        return null;
    }

    private RestyWrapper getAuthAPI(Vehicle v) {
        String authToken = v.getStreamingToken();

        // This call requires BASIC authentication using the user name (this is
        // the user's registered email address) and the authToken.
        // We can't use the Resty authentication mechanism because the tesla
        // site doesn't seem to request authentication - it just expects the
        // Authorization header feld to be present.
        // To accomplish that, create a new (temporary) Resty instance and
        // add the auth header to it.
        RestyWrapper api = new RestyWrapper(ReadTimeoutInMillis);
        setAuthHeader(api, v.getContext().getUsername(), authToken);
        return api;
    }

}