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
    
    public State state;
    
/*------------------------------------------------------------------------------
 *
 * Constants and Enums
 * 
 *----------------------------------------------------------------------------*/
    private enum Keys {
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
    
    
/*==============================================================================
 * -------                                                               -------
 * -------              Public Interface To This Class                   ------- 
 * -------                                                               -------
 *============================================================================*/
    
    // Accessors
    
    @Override public String getStateName() { return "Unstreamed State"; }
    
    @Override protected BaseState setState(boolean valid) {
        return (state = valid ? new State(this) : null);
    }
    
    // Constructors
    public SnapshotState(Vehicle v) {
        super(v);
    }
    

/*------------------------------------------------------------------------------
 *
 * Methods overridden from APICall
 * 
 *----------------------------------------------------------------------------*/
    
    public boolean opportunisticRefresh() {
        // Just try reading. Maybe we're lucky enough to still be connected
        if (getFromStream()) return true;

        // Well, that didn't work! Get a new reader and try again
        locationReader = refreshReader();
        if (getFromStream()) return true;
        
        return false;
    }
    
    public boolean refreshFromStream() {
        return getFromStream();
    }
    
    @Override public boolean refresh() {
        locationReader = refreshReader();
        return getFromStream();
    }
    
    
/*------------------------------------------------------------------------------
 *
 * Methods overridden from Object
 * 
 *----------------------------------------------------------------------------*/
    
    
    @Override public String toString() {
        if (state == null) return "[ ]";
        return String.format(
                "Time Stamp: %s (%s)\n" +
                "Speed: %3.1f\n" +
                "Location: [(Lat: %f, Lng: %3f), Heading: %d, Elevation: %d]\n" +
                "Charge Info: [SoC: %d, Power: %d]\n" +
                "Odometer: %7.1f\n" +
                "Range: %d\n",
                state.vehicleTimestamp, new Date(state.vehicleTimestamp),
                state.speed,
                state.estLat, state.estLng, state.estHeading, state.elevation,
                state.soc, state.power,
                state.odometer, state.range
                // Don't know what shift_state is! Always seems to be null
                );
    }
    
/*------------------------------------------------------------------------------
 *
 * Methods for setting up the Streaming connection and reading the data
 * 
 *----------------------------------------------------------------------------*/

    private boolean getFromStream () {
        JSONObject val = produce();
        if (val == null) {
            state = null;
            return false;
        }
        setJSONState(val);
        state = new State(this);
        state.timestamp = System.currentTimeMillis();
        return true;
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
                if (msg.contains("GET: [401] Unauthorized") ||
                    msg.contains("GET: [401] provide valid authentication") ||
                    msg.contains("Stream closed")) {
                    Tesla.logger.log(Level.INFO, "Getting new token due to problem reading stream: " + msg);
                    refreshAuthentication();
                    if (authenticatedVehicle == null) break;
                    rw = getAuthAPI(authenticatedVehicle);
                } else {
                    Tesla.logger.warning("Snapshot GET failed: " + e);
                }
            }
            Utils.sleep(500);
        }
        
        Tesla.logger.warning("Tried 5 times to establish a Snapshot stream - giving up");
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

        ActionController a = new ActionController(v);
        for (int i = 0; i < WakeupRetries; i++) {

            List<Vehicle> vList = new ArrayList<>();
            v.getContext().fetchVehiclesInto(vList);
            for (Vehicle newV : vList) {
                if (newV.getVID().equals(vid) && newV.getStreamingToken() != null) {
                    authenticatedVehicle = newV;
                    return;
                }
            }
            a.wakeUp(); Utils.sleep(500);
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
        setAuthHeader(api, v.getContext().getUsername(), authToken);
        return api;
    }
    
/*------------------------------------------------------------------------------
 *
 * The State object
 * 
 *----------------------------------------------------------------------------*/
    
    public static class State extends BaseState {
        public long   vehicleTimestamp;
        public double speed;
        public double odometer;
        public int    soc;
        public int    elevation;
        public int    estHeading;
        public int    heading;
        public double estLat;
        public double estLng;
        public int    power;
        public String shiftState;
        public int    range;
        public int    estRange;
        
        public State(SnapshotState ss) {
            vehicleTimestamp = ss.getLong(Keys.timestamp);
            speed = ss.getDouble(Keys.speed);
            if (Double.isNaN(speed)) speed = 0.0;
            odometer = ss.getDouble(Keys.odometer);
            soc = ss.getInteger(Keys.soc);
            elevation = ss.getInteger(Keys.elevation);
            estHeading = ss.getInteger(Keys.est_heading);
            heading = ss.getInteger(Keys.heading);
            estLat = ss.getDouble(Keys.est_lat);
            estLng = ss.getDouble(Keys.est_lng);
            power = ss.getInteger(Keys.power);
            shiftState = ss.getString(Keys.shift_state);
            range = ss.getInteger(Keys.range);
            estRange = ss.getInteger(Keys.est_range);
        }
    }
}