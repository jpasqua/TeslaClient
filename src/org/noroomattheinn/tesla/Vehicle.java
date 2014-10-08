/*
 * Vehicle.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 5, 2013
 */

package org.noroomattheinn.tesla;

import java.io.IOException;
import java.util.logging.Level;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.web.JSONResource;

/**
 * Vehicle: This object represents a single Tesla Vehicle. It provides information
 * describing the vehicle and provides query calls to get the current state
 * of various types/
 * <P>
 * A good running description of the overall Tesla REST API is given in this 
 * <a href="http://goo.gl/Z1Lul" target="_blank">Google Doc</a>. More notes and
 * a mockup can also be found at
 * <a href="http://docs.timdorr.apiary.io" target="_blank">docs.timdorr.apiary.io</a>.
 * 
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public class Vehicle {
/*------------------------------------------------------------------------------
 *
 * Constants and Enums
 * 
 *----------------------------------------------------------------------------*/
    public static enum StateType {Charge, Drive, GUI, HVAC, Vehicle};
    
    // The following are effectively constants, but are set in the constructor
    private final String    ChargeEndpoint;
    private final String    DriveEndpoint;
    private final String    GUIEndpoint;
    private final String    HVACEndpoint;
    private final String    VehicleStateEndpoint;

/*------------------------------------------------------------------------------
 *
 * Internal State
 * 
 *----------------------------------------------------------------------------*/
    private final Tesla         tesla;
    private final Streamer      streamer;

    // Instance variables that describe the Vehicle
    private final String        color;
    private final String        streamingVID;
    private final String        userID;
    private final String        vehicleID;
    private final String        vin;
    private final String        streamingTokens[];
    private final String        status;
    private final Options       options;
    private final String        baseValues;
    private final String        displayName;
    private final boolean       remoteStartEnabled;
    private final boolean       notificationsEnabled;
    private final boolean       calendarEnabled;
    

/*==============================================================================
 * -------                                                               -------
 * -------              Public Interface To This Class                   ------- 
 * -------                                                               -------
 *============================================================================*/
    
    public Vehicle(Tesla tesla, JSONObject description) {
        this.tesla = tesla;
        this.baseValues = description.toString();
        
        color = description.optString("color");
        displayName = description.optString("display_name");
        vehicleID = description.optString("id");
        userID = description.optString("user_id");
        streamingVID = description.optString("vehicle_id");
        vin = description.optString("vin");
        status = description.optString("state");
        remoteStartEnabled = description.optBoolean("remote_start_enabled");
        notificationsEnabled = description.optBoolean("notifications_enabled");
        calendarEnabled = description.optBoolean("calendar_enabled");

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
        streamer = new Streamer(this);
        
        // Initialize state endpoints
        ChargeEndpoint = Tesla.vehicleData(vehicleID, "charge_state");
        DriveEndpoint = Tesla.vehicleData(vehicleID, "drive_state");
        GUIEndpoint = Tesla.vehicleData(vehicleID, "gui_settings");
        HVACEndpoint = Tesla.vehicleData(vehicleID, "climate_state");
        VehicleStateEndpoint = Tesla.vehicleData(vehicleID, "vehicle_state");
    }
    
    
/*------------------------------------------------------------------------------
 *
 * Methods to access basic Vehicle information
 * 
 *----------------------------------------------------------------------------*/
    
    public String   getVIN() { return vin; }
    public String   getVID() { return vehicleID; }
    public String   getStreamingVID() { return streamingVID; }
    public String   status() { return status; } // Status can be "asleep", "waking", or "online"
    public Options  getOptions() { return options; }
    public String   getStreamingToken() { return streamingTokens[0]; }
    public String   getDisplayName() { return displayName; }
    public boolean  remoteStartEnabled() { return remoteStartEnabled; }
    public boolean  notificationsEnabled() { return notificationsEnabled; }
    public boolean  calendarEnabled() { return calendarEnabled; }
    public String   getUnderlyingValues() { return baseValues; }
    public boolean  isAwake() { return tesla.isCarAwake(this); }
    public boolean  isAsleep() { return !isAwake(); }
    public boolean  mobileEnabled() {
        JSONObject r = tesla.getState(Tesla.vehicleSpecific(vehicleID, "mobile_enabled"));
        return r.optBoolean("reponse", false);
    }

/*------------------------------------------------------------------------------
 *
 * Methods to query various types of Vehicle state
 * 
 *----------------------------------------------------------------------------*/
    
    public BaseState query(StateType which) {
        switch (which) {
            case Charge: return queryCharge();
            case Drive: return queryDrive();
            case GUI: return queryGUI();
            case HVAC: return queryHVAC();
            case Vehicle: return queryVehicle();
            default:
                Tesla.logger.severe("Unexpected query type: " + which);
                return null;
        }
    }
    
    public ChargeState queryCharge() {
        return new ChargeState(tesla.getState(ChargeEndpoint));
    }
    public DriveState queryDrive() {
        return new DriveState(tesla.getState(DriveEndpoint));
    }
    public GUIState queryGUI() {
        return new GUIState(tesla.getState(GUIEndpoint));
    }
    public HVACState queryHVAC() {
        return new HVACState(tesla.getState(HVACEndpoint));
    }
    public VehicleState queryVehicle() {
        return new VehicleState(tesla.getState(VehicleStateEndpoint));
    }
    public Streamer getStreamer() { return streamer; }

/*------------------------------------------------------------------------------
 *
 * Utility Methods
 * 
 *----------------------------------------------------------------------------*/
    
    public Tesla tesla() { return tesla; }
    
    @Override public String toString() {
        return String.format(
                "VIN: %s\n" +
                "status: %s\n" +
                "options: [\n%s]",
                vin, status, options.toString()
            );
    }

}
