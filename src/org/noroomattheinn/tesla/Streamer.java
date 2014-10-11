/*
 * Streamer.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 11, 2013
 */

package org.noroomattheinn.tesla;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
 * Streamer: Provides access to streaming information about the current
 * state of the vehicle.
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public class Streamer {
/*------------------------------------------------------------------------------
 *
 * Constants and Enums
 * 
 *----------------------------------------------------------------------------*/
    public enum Keys {
        timestamp, odometer, speed, soc, elevation, est_heading,
        est_lat, est_lng, power, shift_state, range, est_range, heading};
    private final Keys[] keyList = Keys.values();
    private final String allKeys =
            StringUtils.join(keyList, ',', 1, keyList.length);

    private static final String endpointFormat = 
            "https://streaming.vn.teslamotors.com/stream/%s/?values=%s";

    private static final int WakeupRetries = 3;
    private static final int ReadTimeoutInMillis = 25 * 1000;
    
/*------------------------------------------------------------------------------
 *
 * Internal State
 * 
 *----------------------------------------------------------------------------*/
    
    private Vehicle authenticatedVehicle = null;
    private BufferedReader locationReader = null;
    private Vehicle v;
    
/*==============================================================================
 * -------                                                               -------
 * -------              Public Interface To This Class                   ------- 
 * -------                                                               -------
 *============================================================================*/
    
    public Streamer(Vehicle vehicle) {
        this.v = vehicle;
    }
        
    public StreamState opportunisticRefresh() {
        // Just try reading. Maybe we're lucky enough to still be connected
        StreamState state = getFromStream();
        if (state != null) return state;

        // Well, that didn't work! Get a new reader and try again
        locationReader = refreshReader();
        return getFromStream();
    }
    
    public StreamState refreshFromStream() {
        return getFromStream();
    }
    
    public StreamState refresh() {
        locationReader = refreshReader();
        return getFromStream();
    }
    
    
/*------------------------------------------------------------------------------
 *
 * Private methods for setting up the Streaming connection and reading the data
 * 
 *----------------------------------------------------------------------------*/

    private StreamState getFromStream () {
        JSONObject val = produce();
        if (val == null) {
            return null;
        }
        return new StreamState(val);
    }
    
    private BufferedReader refreshReader() {
        // Just try making a connection, maybe we're lucky enough to still be authenticated
        return establishStreamingConnection();
    }
    
    private JSONObject produce() {
        if (locationReader == null) {
            return null;
        }
        
        try {
            String line = locationReader.readLine();
            if (line == null) {
                locationReader = null;
                return null;
            }

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

    private BufferedReader establishStreamingConnection() {
        if (authenticatedVehicle == null) {
            refreshAuthentication();
            if (authenticatedVehicle == null) {
                Tesla.logger.warning("Can't authenticate for streaming!");
                return null;
            }
        }
        
        String endpoint = String.format(
                endpointFormat, authenticatedVehicle.getStreamingVID(), allKeys);
        
        RestyWrapper rw = getAuthAPI(authenticatedVehicle);
        
        for (int i = 0; i < 5; i++) {
            try {
                TextResource r = rw.text(endpoint);
                if (r.status(200)) {
                    return new BufferedReader(new InputStreamReader(r.stream()));
                }
            } catch (IOException e) {
                String msg = e.toString();
                if (msg.contains("[401]") || msg.contains("Stream closed")) {
                    Tesla.logger.log(Level.INFO, "Getting new token: " + msg.trim());
                    refreshAuthentication();
                    if (authenticatedVehicle == null) break;
                    rw = getAuthAPI(authenticatedVehicle);
                } else {
                    Tesla.logger.warning("Stream GET failed: " + e);
                }
            }
            Utils.sleep(500);
        }
        
        Tesla.logger.warning("Tried 5 times to establish a stream - giving up");
        return null;
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


    private void refreshAuthentication() {
        String vid = v.getVID();
        // Remember this so we can find the right vehicle when we fetch
        // the updated list of vehicles after doing the wakeup

        for (int i = 0; i < WakeupRetries; i++) {

            List<Vehicle> vList = v.tesla().queryVehicles();
            if (vList != null) {
                for (Vehicle newV : vList) {
                    if (newV.getVID().equals(vid) && newV.getStreamingToken() != null) {
                        authenticatedVehicle = newV;
                        return;
                    }
                }
            }
            v.wakeUp(); Utils.sleep(500);
        }

        // For some reason we can't get Streaming tokens. We've tried enough - Give up
        Tesla.logger.log(Level.WARNING, "Error: couldn't retreive auth tokens");
        authenticatedVehicle = null;
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
        setAuthHeader(api, v.tesla().getUsername(), authToken);
        return api;
    }
    
}