/*
 * HVACState.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 5, 2013
 */

package org.noroomattheinn.tesla;

import org.noroomattheinn.utils.Utils;

/**
 * HVACState: Retrieves the state of the HVAC system.
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public class HVACState extends StateAPI {

    public State state;
    
    //
    // Constructor
    //
    
    public HVACState(Vehicle v) {
        super(v, Tesla.vehicleData(v.getVID(), "climate_state"), "HVAC State");
    }
    
    @Override protected void setState(boolean valid) {
        state = valid ? new State(this) : null;
    }

    //
    // Override methods
    //
    
    @Override public String toString() {
        return String.format(
            "    Inside Temp: %3.0f\n" +
            "    Outside Temp: %3.0f\n" +
            "    Driver Setpoint: %3.0f\n" +
            "    Passenger Setpoint: %3.0f\n" +
            "    HVAC On: %s\n" +
            "    Front Defroster On: %d\n" +
            "    Rear Defroster On: %s\n" +
            "    Fan Setting: %d\n", 
            Utils.cToF(state.insideTemp),
            Utils.cToF(state.outsideTemp),
            Utils.cToF(state.driverTemp),
            Utils.cToF(state.passengerTemp),
            Utils.yesNo(state.autoConditioning),
            state.isFrontDefrosterOn,
            Utils.yesNo(state.isRearDefrosterOn),
            state.fanStatus
            );
    }
    
    public static class State extends BaseState {
        public double  insideTemp;
        public double  outsideTemp;
        public double  driverTemp;
        public double  passengerTemp;
        public boolean autoConditioning;
        public int     isFrontDefrosterOn;
        public boolean isRearDefrosterOn;
        public int     fanStatus;
    
        public State(HVACState hvs) {
            insideTemp = hvs.getDouble("inside_temp"); 
            outsideTemp = hvs.getDouble("outside_temp"); 
            driverTemp = hvs.getDouble("driver_temp_setting"); 
            passengerTemp = hvs.getDouble("passenger_temp_setting"); 
            autoConditioning = hvs.getBoolean("is_auto_conditioning_on"); 
            isFrontDefrosterOn = hvs.getInteger("is_front_defroster_on"); 
            isRearDefrosterOn = hvs.getBoolean("is_rear_defroster_on"); 
            fanStatus = hvs.getInteger("fan_status"); 
        }
    }
}
