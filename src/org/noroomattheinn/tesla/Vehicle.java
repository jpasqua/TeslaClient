/*
 * Vehicle.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 5, 2013
 */

package org.noroomattheinn.tesla;

import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import org.noroomattheinn.utils.Utils;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;

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
    public enum PanoCommand {open, comfort, vent, close};

    // The following are effectively constants, but are set in the constructor
    private final String    ChargeEndpoint, DriveEndpoint, GUIEndpoint,
                            HVACEndpoint, VehicleStateEndpoint;
    private final String    HVAC_Start, HVAC_Stop, HVAC_SetTemp;
    private final String    Charge_Start, Charge_Stop, Charge_SetMax,
                            Charge_SetStd, Charge_SetPct;
    private final String    Doors_OpenChargePort, Doors_Unlock, Doors_Lock,
                            Doors_Sunroof, Doors_Trunk;
    private final String    Action_Honk, Action_Flash, Action_Wakeup, Action_RemoteStart;


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
        
        // Initialize HVAC endpoints
        HVAC_Start = Tesla.vehicleCommand(vehicleID, "auto_conditioning_start");
        HVAC_Stop = Tesla.vehicleCommand(vehicleID, "auto_conditioning_stop");
        HVAC_SetTemp = Tesla.vehicleCommand(vehicleID, "set_temps");
        
        // Initialize Charge endpoints
        Charge_Start = Tesla.vehicleCommand(vehicleID, "charge_start");
        Charge_Stop = Tesla.vehicleCommand(vehicleID, "charge_stop");
        Charge_SetMax = Tesla.vehicleCommand(vehicleID, "charge_max_range");
        Charge_SetStd = Tesla.vehicleCommand(vehicleID, "charge_standard");
        Charge_SetPct = Tesla.vehicleCommand(vehicleID, "set_charge_limit");
        
        // Initialize Door endpoints
        Doors_OpenChargePort = Tesla.vehicleCommand(vehicleID, "charge_port_door_open");
        Doors_Unlock = Tesla.vehicleCommand(vehicleID, "door_unlock");
        Doors_Lock = Tesla.vehicleCommand(vehicleID, "door_lock");
        Doors_Sunroof = Tesla.vehicleCommand(vehicleID, "sun_roof_control");
        Doors_Trunk = Tesla.vehicleCommand(vehicleID, "trunk_open");
        
        // Initialize Action Endpoints
        Action_Honk = Tesla.vehicleCommand(vehicleID, "honk_horn");
        Action_Flash = Tesla.vehicleCommand(vehicleID, "flash_lights");
        Action_RemoteStart = Tesla.vehicleCommand(vehicleID, "remote_start_drive");
        Action_Wakeup = Tesla.vehicleSpecific(vehicleID, "wake_up");        
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
    public boolean  isAsleep() { return !isAwake(); }
    public boolean  mobileEnabled() {
        JSONObject r = tesla.getState(Tesla.vehicleSpecific(vehicleID, "mobile_enabled"));
        return r.optBoolean("reponse", false);
    }
    public boolean isAwake() {
        List<Vehicle> vehicles = tesla.queryVehicles();
        if (vehicles != null) {
            for (Vehicle v : vehicles) {
                if (vin.equals(v.vin)) {
                    return !v.status().equals("asleep");
                }
            }
        }
        return false;
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
 * Methods to control the HVAC system
 * 
 *----------------------------------------------------------------------------*/
    
    public Result setAC(boolean on) {
        if (on) return startAC();
        else return stopAC();
    }
    
    public Result startAC() {
        return new Result(tesla.invokeCommand(HVAC_Start));
    }

    public Result stopAC() {
        return new Result(tesla.invokeCommand(HVAC_Stop));
    }
    
    public Result setTempC(double driverTemp, double passengerTemp) {
        String tempsPayload = String.format(Locale.US,
                "{'driver_temp' : '%3.1f', 'passenger_temp' : '%3.1f'}",
                driverTemp, passengerTemp);
        return new Result(tesla.invokeCommand(HVAC_SetTemp, tempsPayload));
    }
    
    public Result setTempF(double driverTemp, double passengerTemp) {
        return setTempC(Utils.fToC(driverTemp), Utils.fToC(passengerTemp));
    }

/*------------------------------------------------------------------------------
 *
 * Methods to control the Charging system
 * 
 *----------------------------------------------------------------------------*/
    
    public Result setChargeState(boolean charging) {
        return new Result(tesla.invokeCommand(charging? Charge_Start : Charge_Stop));
    }
    
    public Result startCharing() { return setChargeState(true); }

    public Result stopCharing() { return setChargeState(false); }
    
    public Result setChargeRange(boolean max) {
        return new Result(tesla.invokeCommand(max ? Charge_SetMax : Charge_SetStd));
    }
    
    public Result setChargePercent(int percent) {
        if (percent < 1 || percent > 100)
            return new Result(false, "value out of range");
        JSONObject response = tesla.invokeCommand(
                Charge_SetPct,  String.format("{'percent' : '%d'}", percent));
        if (response.optString("reason").equals("already_set")) {
            try {
                response.put("result", true);
            } catch (JSONException e) {
                Tesla.logger.severe("Can't Happen!");
            }
        }
        return new Result(response);
    }
    
/*------------------------------------------------------------------------------
 *
 * Methods to control the doors, trunk, frunk, and roof
 * 
 *----------------------------------------------------------------------------*/
    
    public Result setLockState(boolean locked) {
        return new Result(tesla.invokeCommand(locked ? Doors_Lock : Doors_Unlock));
    }
    
    public Result lockDoors() { return setLockState(true); }

    public Result unlockDoors() { return setLockState(false); }
    
    public Result openChargePort() {
        return new Result(tesla.invokeCommand(Doors_OpenChargePort));
    }
    
    public Result openFrunk() { // Requires 6.0 or greater
        return new Result(tesla.invokeCommand(Doors_Trunk, "{'whichTrunk' : 'front'}"));
    }
    
    public Result openTrunk() { // Requires 6.0 or greater
        return new Result(tesla.invokeCommand(Doors_Trunk, "{'whichTrunk' : 'rear'}"));
    }
    
    public Result setPano(PanoCommand cmd) {
        String payload = String.format("{'state' : '%s'}", cmd.name());
        return new Result(tesla.invokeCommand(Doors_Sunroof, payload));
    }
    
    public Result stopPano() {
        return new Result(tesla.invokeCommand(Doors_Sunroof, "{'state' : 'stop'}"));
    }
    
/*------------------------------------------------------------------------------
 *
 * Methods to perform miscellaneous actions
 * 
 *----------------------------------------------------------------------------*/
    
    public Result honk() {
        return new Result(tesla.invokeCommand(Action_Honk));
    }

    public Result flashLights() {
        return new Result(tesla.invokeCommand(Action_Flash));
    }

    public Result remoteStart(String password) {
        return new Result(tesla.invokeCommand(
                Action_RemoteStart, "{'password' : '" + password + "'}"));
    }

    public Result wakeUp() {
        return new Result(tesla.invokeCommand(Action_Wakeup));
    }
    
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
