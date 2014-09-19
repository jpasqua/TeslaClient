/*
 * APICall.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 5, 2013
 */

package org.noroomattheinn.tesla;

import java.io.IOException;
import org.noroomattheinn.utils.RestyWrapper;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.web.Content;
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
    // Instance Variables
    private final RestyWrapper  api;
    private final String        apiName;
    private JSONObject          jsonResponse;

    protected final Vehicle     v;
    
    //
    // Constructors
    //
    
    public APICall(Vehicle v, String apiName) {
        this.v = v;
        this.api = v.getAPI();
        this.jsonResponse = new JSONObject();
        this.apiName = apiName;
    }
    
    //
    // Updating the endpoint and refreshing the state
    //
    
    public boolean getState(String state) { return call(state, null); }
    
    public boolean invokeCommand(String command) { return invokeCommand(command, "{}"); }
        
    public boolean invokeCommand(String command, String payload) {
        try {   
            return call(command, Resty.content(new JSONObject(payload)));
        } catch (JSONException ex) {
            Tesla.logger.severe("JSON Syntax Error: " + payload);
            return false;
        }
    }
    
    private boolean call(String command, Content payload) {
        try {
            JSONObject response;
            if (payload == null)
                response = api.json(command).object().getJSONObject("response");
            else
                response = api.json(command, payload).object().getJSONObject("response");
            setJSONState(response);
            return true;
        } catch (IOException | JSONException ex) {
            Tesla.logger.warning("Failed invoking (" + apiName + "): " + ex);
            jsonResponse = new JSONObject();
            return false;
        }
    }
    
    
    public JSONObject getRawResult() { return this.jsonResponse; }
    
    protected void setJSONState(JSONObject newState) {
        this.jsonResponse = newState;
    }

    public Vehicle getVehicle() { return v; }
    public String getAPIName() { return apiName; }
    
    //
    // Field Accessor Methods
    //
    
    public String   getString(String key)  { return jsonResponse.optString(key);  }
    public boolean  getBoolean(String key) { return jsonResponse.optBoolean(key); }
    public double   getDouble(String key)  { return jsonResponse.optDouble(key);  }
    public int      getInteger(String key) { return jsonResponse.optInt(key);     }
    public long     getLong(String key)    { return jsonResponse.optLong(key);    }
    
    public String   getString(Enum<?> key)  { return jsonResponse.optString(key.name()); }
    public boolean  getBoolean(Enum<?> key) { return jsonResponse.optBoolean(key.name());}
    public double   getDouble(Enum<?> key)  { return jsonResponse.optDouble(key.name()); }
    public int      getInteger(Enum<?> key) { return jsonResponse.optInt(key.name()); }
    public long     getLong(Enum<?> key)    { return jsonResponse.optLong(key.name());}

    
    //
    // Overrides
    //
    
    @Override public String toString() { 
        try {
            return jsonResponse.toString(4);
        } catch (JSONException ex) {
            return jsonResponse.toString();
        }
    }
    
}
