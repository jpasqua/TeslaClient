/*
 * APICall.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 5, 2013
 */

package org.noroomattheinn.tesla;

import java.io.IOException;
import java.util.logging.Level;
import org.noroomattheinn.utils.RestyWrapper;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

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
    
    // Instance Variables
    private RestyWrapper api;
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
            if (endpoint != null) {
                setState(api.json(endpoint).object());
            }
            return true;
        } catch (IOException | JSONException ex) {
            Tesla.logger.log(Level.FINEST, "Failed refreshing. HANDLED.", ex);
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
    
}
