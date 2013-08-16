/*
 * Vehicle.java - Copyright(c) 2013  All Rights Reserved, Joe Pasqua
 * Created: Jul 5, 2013
 */

package org.noroomattheinn.tesla;

import java.io.IOException;
import java.util.logging.Level;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.web.JSONResource;
import us.monoid.web.Resty;

/**
 * Vehicle: This object represents a single Tesla Vehicle. All access to
 * the vehicle state and actions is performed via the vehicleID contained
 * in this object.
 * <P>
 * A good running description of the overall Tesla REST API is given in this 
 * <a href="http://goo.gl/Z1Lul" target="_blank">Google Doc</a>. More notes and
 * a mockup can also be found at
 * <a href="http://docs.timdorr.apiary.io" target="_blank">docs.timdorr.apiary.io</a>.
 * 
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public class Vehicle {
    // Instance variables for the context in which this object was created
    private Tesla   tesla;
    private Resty   api;

    // Instance variables that describe a Vehicle
    private String      UNKNOWN_color;
    private String      UNKNOWN_display_name;
    private String      streamingVID;
    private String      userID;
    private String      vehicleID;
    private String      vin;
    private String      streamingTokens[];
    private boolean     online;
    private Options     options;
    private GUIState    lastKnownGUIState;
                
    
    //
    // Constructors
    //
    
    public Vehicle(Tesla tesla, JSONObject description) {
        this.tesla = tesla;
        this.api = tesla.getAPI();
        UNKNOWN_color = "UNKNOWN";
        UNKNOWN_display_name = "UNKNOWN";
        vehicleID = description.optString("id");
        userID = description.optString("user_id");
        streamingVID = description.optString("vehicle_id");
        vin = description.optString("vin");
        String state = description.optString("state");
        online = (state != null && state.equals("online"));
        
        // Get the streaming tokens if they exist...
        streamingTokens = new String[2];
        try {
            JSONArray tokens = description.getJSONArray("tokens");
            if (tokens != null && tokens.length() == 2) {
                streamingTokens[0] = tokens.getString(0);
                streamingTokens[1] = tokens.getString(1);
            } 
        } catch (JSONException ex) {
            Tesla.logger.log(Level.SEVERE, null, ex);
        }
                
        // Handle the Options
        options = new Options(description.optString("option_codes"));
        
        // Some options don't tend to change much, so get a copy here for reference
        lastKnownGUIState = new GUIState(this);
        while (lastKnownGUIState.refresh() == false)  {}
    }
    
    
    //
    // Field Accessor Methods
    //
    
    public String   getVIN() { return vin; }
    public String   getVID() { return vehicleID; }
    public String   getStreamingVID() { return streamingVID; }
    public boolean  online() { return online; }
    public Options  getOptions() { return options; }
    public String   getStreamingToken() { return streamingTokens[0]; }
    public GUIState getLastKnownGUIState() { return lastKnownGUIState; }
    public boolean  mobileEnabled() {
        try {
            JSONResource resource = api.json(Tesla.endpoint(vehicleID, "mobile_enabled"));
            return resource.object().getBoolean("result");
        } catch (IOException | JSONException e) {
            return false;
        }
    }
    
    
    //
    // Methods to get context
    //
    
    public Resty getAPI() { return api; }
    public Tesla getContext() { return tesla; }
    
    
    //
    // Override methods
    //
    
    @Override
    public String toString() {
        return String.format(
                "VIN: %s\n" +
                "online: %b\n" +
                "options: [\n%s]",
                vin, online, options.toString()
            );
    }

}
