/*
 * APICall.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 5, 2013
 */

package org.noroomattheinn.tesla;

import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.web.Resty;

/**
 * APICall: This class is the parent of all API interactions for State and
 * Control. It encapsulates the Vehicle for the interaction and the REST
 * endpoint to be invoked. The refresh method can be used to reinvoke the
 * API; for example to refresh a state object. It can also be used to update
 * the desired endpoint or parameters.
 * <P>
 * The object also encapsulates the JSON return value from the API call.
 * Elements of that JSON value can be retrieved by name and type using the 
 * getTYPE(key) methods.
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public abstract class APICall {
    private static double MaxRequestRate = 20.0 / (1000.0 * 60.0);  // 20 requests per minute
    private static long startTime = new Date().getTime();
    private static long requests = 0;
    private static final Logger logger = Logger.getLogger(APICall.class.getName());
    
    // Instance Variables
    private Resty       api;
    private String      vid;
    private JSONObject  theState;
    private String      endpoint;
    private boolean     hasValidData;
    protected Vehicle   v;
    
    //
    // Constructors
    //
    
    public APICall(Vehicle v, String endpoint) {
        this(v);
        this.endpoint = endpoint;
    }
    
    public APICall(Vehicle v) {
        this.v = v;
        this.api = v.getAPI();
        this.vid = v.getVID();
        this.endpoint = null;
        this.theState = new JSONObject();
        this.hasValidData = false;  // Has never been refreshed (successfully)
    }
    
    
    //
    // Updating the endpoint and refreshing the state
    //
    
    public final boolean setAndRefresh(String newEndpoint) {
        setEndpoint(newEndpoint);
        return refresh();
    }
    
    public boolean refresh() {
        try {
            honorRateLimit();
            if (endpoint != null)  setState(api.json(endpoint).object());
            requests++;
            return true;
        } catch (IOException | JSONException ex) {
            Tesla.logger.log(Level.FINEST, null, ex);
            theState = new JSONObject();
            hasValidData = false;
            return false;
        }
    }
    
    protected void invalidate() { hasValidData = false; }
    
    protected void setState(JSONObject newState) {
        this.theState = newState;
        hasValidData = true;
    }

    private void setEndpoint(String newEndpoint) { this.endpoint = newEndpoint; }

    public String getStateName() { return "State"; }
    public final boolean hasValidData() { return hasValidData; }
    public Vehicle getVehicle() { return v; }
    //
    // Field Accessor Methods
    //
    
    public String   getString(String key)  { return theState.optString(key);  }
    public boolean  getBoolean(String key) { return theState.optBoolean(key); }
    public double   getDouble(String key)  { return theState.optDouble(key);  }
    public int      getInteger(String key) { return theState.optInt(key);     }
    public long     getLong(String key)    { return theState.optLong(key);    }
    
    public String   getString(Enum<?> key) { return theState.optString(key.name()); }
    public boolean  getBoolean(Enum<?> key) { return theState.optBoolean(key.name());}
    public double   getDouble(Enum<?> key)  { return theState.optDouble(key.name()); }
    public int      getInteger(Enum<?> key) { return theState.optInt(key.name()); }
    public long     getLong(Enum<?> key)    { return theState.optLong(key.name());}

    
    //
    // Overrides
    //
    
    public String toString() { 
        try {
            return theState.toString(4);
        } catch (JSONException ex) {
            return theState.toString();
        }
    }
    
    private void honorRateLimit() {
        while (true) {
            if (requests < 30) return;  // Don't worry too much until there is some history
            
            long now = new Date().getTime();
            long elapsedMillis = now - startTime;
            double rate = ((double) requests) / elapsedMillis;
            if (rate > MaxRequestRate) {
                try {
                    logger.log(
                        Level.INFO, "Throttling request rate. Requests: {0}, Millis: {1}\n",
                        new Object[]{requests, elapsedMillis});
                    Thread.sleep(  (long) (((double)requests)/MaxRequestRate +  startTime - now ) );
                } catch (InterruptedException ex) {
                    logger.log(Level.SEVERE, null, ex);
                }
            } else return;
        }
    }
    
}
