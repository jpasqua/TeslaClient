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
public class VehicleState extends StateAPI {
    
    public State state;
    
    //
    // Constructors
    //
    
    public VehicleState(Vehicle v) {
        super(v, Tesla.vehicleData(v.getVID(), "vehicle_state"), "Vehicle State");
    }
    
    @Override protected void setState(boolean valid) {
        state = valid ? new State(this) : null;
    }
    
    
    //
    // Override Methods
    //
    
    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        if (state.isDFOpen) sb.append("    Driver Front Door is open\n");
        if (state.isDROpen) sb.append("    Driver Rear Door is open\n");
        if (state.isPFOpen) sb.append("    Passenger Front Door is open\n");
        if (state.isPROpen) sb.append("    Passenger Rear Door is open\n");
        if (state.isFTOpen) sb.append("    Frunk is open\n");
        if (state.isRTOpen) sb.append("    Trunk is open\n");
        sb.append("    The car is "); 
        sb.append(state.locked ? "locked\n" : "unlocked\n");
        if (state.hasPano) {
            sb.append("    Panoramic roof: ");
            if (state.panoState != VehicleState.PanoPosition.unknown)
                sb.append(state.panoState);
            sb.append(" ("); sb.append(state.panoPercent); sb.append("%)\n");
        }
        sb.append("    Firmware version: "); sb.append(state.version);
        return sb.toString();
    }
    
    
    //
    // Nested Classes
    //
    
    public enum PanoPosition {open, closed, vent, comfort, moving, unknown, Unknown};

    public static class State extends BaseState {
        public boolean  isDFOpen;
        public boolean  isPFOpen;
        public boolean  isDROpen;
        public boolean  isPROpen;
        public boolean  isFTOpen;
        public boolean  isRTOpen;
        public boolean  locked;
        public boolean  hasPano;
        public int      panoPercent;
        public PanoPosition panoState;
        public String   version;
        public boolean  hasDarkRims;
        public String   wheelType;
        public boolean  hasSpoiler;
        public String   roofColor;
        public String   perfConfig;

        public State(VehicleState vs) {
            isDFOpen = vs.getInteger("df") != 0;
            isPFOpen = vs.getInteger("pf") != 0;
            isDROpen = vs.getInteger("dr") != 0;
            isPROpen = vs.getInteger("pr") != 0;
            isFTOpen = vs.getInteger("ft") != 0;
            isRTOpen = vs.getInteger("rt") != 0;
            locked = vs.getBoolean("locked");
            hasPano = vs.getBoolean("sun_roof_installed");
            panoPercent = vs.getInteger("sun_roof_percent_open");
            panoState = Utils.stringToEnum(PanoPosition.class, vs.getString("sun_roof_state"));

            version = vs.getString("car_version");
            hasDarkRims = vs.getBoolean("dark_rims");
            wheelType = vs.getString("wheel_type");
            hasSpoiler = vs.getBoolean("has_spoiler");
            roofColor = vs.getString("roof_color");
            perfConfig = vs.getString("perf_config");
        }
    }
}
