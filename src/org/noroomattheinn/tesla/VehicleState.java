/*
 * VehicleState.java - Copyright(c) 2013  All Rights Reserved, Joe Pasqua
 * Created: Jul 5, 2013
 */

package org.noroomattheinn.tesla;

/**
 * VehicleState: This object provides information about the current state of the 
 * vehicle and also some of the configuration options.
 * <p>
 * This uses the vehicle_state endpoint which mixes door state with an odd
 * assortment of other vehicle information. For the purposes of symmetry with
 * DoorController, we have a DoorState object and this VehicleState object
 * for the other odds and ends. Note that some of these items overlap with
 * information in the main Vehicle object.
 * 
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */
public class VehicleState extends APICall {
    
    //
    // Constructors
    //
    
    public VehicleState(Vehicle v) {
        super(v, Tesla.command(v.getVID(), "vehicle_state"));
    }
        
    //
    // Field Accessor Methods
    //
    
    public String getStateName() { return "Vehicle State"; }

    public String  version()     { return getString("car_version"); }
    public boolean hasDarkRims() { return getBoolean("dark_rims"); }
    public String  wheelType()   { return getString("wheel_type"); }
    public boolean hasSpoiler()  { return getBoolean("has_spoiler"); }
    public String  roofColor()   { return getString("roof_color"); }
    public String  perfConfig()  { return getString("perf_config"); }
    
}
