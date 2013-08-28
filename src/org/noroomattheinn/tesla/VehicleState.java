/*
 * VehicleState.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 5, 2013
 */

package org.noroomattheinn.tesla;

import org.noroomattheinn.utils.Utils;

/**
 * VehicleState: Retrieve the state of the doors and panoramic roof.
 * <P>
 * This uses the vehicle_state endpoint which mixes door state with an odd
 * assortment of other vehicle information. For the purposes of symmetry with
 * DoorController, we have this VehicleState object and a separate VehicleState
 * object for the other odds and ends.
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
    
    public String getStateName() { return "Door State"; }
    
    public boolean isDFOpen()       { return getInteger("df") != 0; }
    public boolean isPFOpen()       { return getInteger("pf") != 0; }
    public boolean isDROpen()       { return getInteger("dr") != 0; }
    public boolean isPROpen()       { return getInteger("pr") != 0; }
    public boolean isFTOpen()       { return getInteger("ft") != 0; }
    public boolean isRTOpen()       { return getInteger("rt") != 0; }
    public boolean locked()         { return getBoolean("locked"); }
    public boolean hasPano()        { return getBoolean("sun_roof_installed"); }
    public int     panoPercent()    { return getInteger("sun_roof_percent_open"); }
    public PanoPosition panoState() {
        return Utils.stringToEnum(PanoPosition.class, getString("sun_roof_state")); }

    public String  version()     { return getString("car_version"); }
    public boolean hasDarkRims() { return getBoolean("dark_rims"); }
    public String  wheelType()   { return getString("wheel_type"); }
    public boolean hasSpoiler()  { return getBoolean("has_spoiler"); }
    public String  roofColor()   { return getString("roof_color"); }
    public String  perfConfig()  { return getString("perf_config"); }

    
    //
    // Override Methods
    //
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (isDFOpen()) sb.append("    Driver Front Door is open\n");
        if (isDROpen()) sb.append("    Driver Rear Door is open\n");
        if (isPFOpen()) sb.append("    Passenger Front Door is open\n");
        if (isPROpen()) sb.append("    Passenger Rear Door is open\n");
        if (isFTOpen()) sb.append("    Frunk is open\n");
        if (isRTOpen()) sb.append("    Trunk is open\n");
        sb.append("    The car is "); 
        sb.append(locked() ? "locked\n" : "unlocked\n");
        if (hasPano()) {
            sb.append("    Panoramic roof: ");
            if (panoState() != VehicleState.PanoPosition.unknown)
                sb.append(panoState());
            sb.append(" ("); sb.append(panoPercent()); sb.append("%)\n");
        }
        sb.append("    Firmware version: " + version());
        return sb.toString();
    }
    
    
    //
    // Nested Classes
    //
    
    public enum PanoPosition {open, closed, vent, comfort, moving, unknown, Unknown};

}
