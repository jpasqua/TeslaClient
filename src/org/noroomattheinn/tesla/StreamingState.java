/*
 * StreamingState.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 11, 2013
 */

package org.noroomattheinn.tesla;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.web.Resty;
import us.monoid.web.TextResource;

/**
 * StreamingState: Provides access to streaming information about the current
 * state of the vehicle.
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public class StreamingState extends APICall {
    // Class Variables
    private static BlockingQueue<JSONObject> queue = new ArrayBlockingQueue<>(25);    
    private static Thread producerThread = null;
    private static JSONObject EmptyJSON = constructEmpty();
    
    
    //
    // Field Accessor Methods
    //
    
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

    
    //
    // Constructors
    //
    
    public StreamingState(Vehicle v) {
        super(v);
    }
    

    // Accessors
    public String getStateName() { return "Streaming State"; }
    
    // Update Methods
    
    public boolean refresh() {
        return refresh(5000);
    }
    
    public boolean refresh(int waitMillis) {
        ensureProducer();
        
        try {
            JSONObject rawValues = queue.poll(waitMillis, TimeUnit.MILLISECONDS);
            if (rawValues != null && rawValues != EmptyJSON) {
                setState(rawValues);
                return true;
            }
        } catch (InterruptedException ex) {
            Tesla.logger.log(Level.INFO, null, ex);
        }
        invalidate();
        return false;
    }
    
    
    //
    // Override Methods
    //
    
    public String toString() {
        return String.format(
                "Time Stamp: %s\n" +
                "Speed: %3.1f\n" +
                "Location: [(Lat: %f, Lng: %3f), Heading: %d, Elevation: %d]\n" +
                "Charge Info: [SoC: %d, Power: %d]\n" +
                "Odometer: %7.1f\n" +
                "Range: %d\n",
                timestamp(),
                speed(),
                estLat(), estLng(), estHeading(), elevation(),
                soc(), power(),
                odometer(), range()
                // Don't know what shift_state is! Always seems to be null
                );
    }

    
    //
    // Utility Methods
    //
    
    private synchronized void ensureProducer() {
        if (producerThread == null || producerThread.getState() == Thread.State.TERMINATED) {
            producerThread = new Thread(new Producer(v, queue));
            producerThread.start();
        }
    }

    private static JSONObject constructEmpty() {
        JSONObject jo = new JSONObject();
        try {
            jo.put(Keys.timestamp, "0");
            jo.put(Keys.speed, "0");
            jo.put(Keys.odometer, "0");
            jo.put(Keys.soc, "0");
            jo.put(Keys.elevation, "0");
            jo.put(Keys.est_heading, "0");
            jo.put(Keys.est_lat, "0");
            jo.put(Keys.est_lng, "0");
            jo.put(Keys.power, "0");
            jo.put(Keys.shift_state, "");
            jo.put(Keys.range, "0");
        } catch (JSONException e) {
            // ASSERT: Can't Happen
        }
        return jo;
    }
    
    
    //
    // Nested Classes
    //
    
    public enum Keys {
        timestamp, odometer, speed, soc, elevation, est_heading,
        est_lat, est_lng, power, shift_state, range};

    class Producer implements Runnable {
        // Constants
        private static final int ReadTimeoutInMillis = 5 * 1000;
        private static final int WakeupRetries = 3;
        private static final String endpointFormat = 
                "https://streaming.vn.teslamotors.com/stream/%s/?values=%s";

        // Instance Variables
        private final BlockingQueue<JSONObject> queue;
        private final Vehicle vehicle;
        private StreamingState.Keys[] keyList = StreamingState.Keys.values();
        private final String allKeys =
                StringUtils.join(keyList, ',', 1, keyList.length);


        //
        // Constructors
        //

        public Producer(Vehicle v, BlockingQueue<JSONObject> q) {
            this.vehicle = v;
            this.queue = q;
        }

        
        //
        // Methods that implement the Producer paradigm
        //
        
        public void run() {
            try {
                BufferedReader reader = prepareToProduce();

                while (true) {
                    JSONObject val = produce(reader);
                    queue.put(val);
                    if (val == EmptyJSON)
                        return;
                }
            } catch (InterruptedException ex) {
                Tesla.logger.log(Level.INFO, null, ex);
            }
        }

        JSONObject produce(BufferedReader reader) {        
            try {
                String line = reader.readLine();
                if (line == null) return EmptyJSON;

                JSONObject jo = new JSONObject();
                String vals[] = line.split(",");
                for (int i = 0; i < keyList.length; i++) {
                    try {
                        jo.put(keyList[i], vals[i]);
                    } catch (JSONException ex) {
                        Tesla.logger.log(Level.SEVERE, "Malformed data", ex);
                        return EmptyJSON;
                    }
                }
                return jo;
            } catch (IOException ex) {
                Tesla.logger.log(Level.FINEST, "Timeouts are expected here...", ex);
                return EmptyJSON;
            }
        }

        //
        // Utility Methods
        //

        private BufferedReader prepareToProduce() {
            Vehicle withToken = getVehicleWithAuthToken(vehicle);
            if (withToken == null)
                return null;

            BufferedReader reader = null;
            try {
                String endpoint = String.format(
                        endpointFormat, withToken.getStreamingVID(), allKeys);

                TextResource r = getAuthAPI(withToken).text(endpoint);
                reader = new BufferedReader(new InputStreamReader(r.stream()));
            } catch (IOException ex) {
                // Timed out or other problem
                Tesla.logger.log(Level.INFO, null, ex);
            }
            return reader;
        }

        private void setAuthHeader(Resty api, String username, String authToken) {
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
                a.wakeUp();

                List<Vehicle> vList = new ArrayList<>();
                basedOn.getContext().fetchVehiclesInto(vList);
                for (Vehicle newV : vList) {
                    if (newV.getVID().equals(vid) && newV.getStreamingToken() != null) {
                        return newV;
                    }
                }
            }

            // For some reason we can't get Streaming tokens. We've tried enough
            // so give up 
            Tesla.logger.log(Level.WARNING, "Error: couldn't retreive auth tokens");
            return null;
        }

        private Resty getAuthAPI(Vehicle v) {
            String authToken = v.getStreamingToken();

            // This call requires BASIC authentication using the user name (this is
            // the user's registered email address) and the authToken.
            // We can't use the Resty authentication mechanism because the tesla
            // site doesn't seem to request authentication - it just expects the
            // Authorization header feld to be present.
            // To accomplish that, create a new (temporary) Resty instance and
            // add the auth header to it.
            Resty api = new Resty(new ReadTimeoutOption());
            setAuthHeader(api, v.getContext().getUsername(), authToken);
            return api;
        }

        private class ReadTimeoutOption extends Resty.Option {
            public void apply(URLConnection aConnection) {
                aConnection.setReadTimeout(ReadTimeoutInMillis);
            }
        }

    }

    
}