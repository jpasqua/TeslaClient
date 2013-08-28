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

public class HVACState extends APICall {

    //
    // Constructor
    //
    
    public HVACState(Vehicle v) {
        super(v, Tesla.command(v.getVID(), "climate_state"));
    }
    
    //
    // Field Accessor Methods
    //
    
    public String getStateName() { return "HVAC State"; }

    public double  insideTemp() { return getDouble("inside_temp"); }
    public double  outsideTemp() { return getDouble("outside_temp"); }
    public double  driverTemp() { return getDouble("driver_temp_setting"); }
    public double  passengerTemp() { return getDouble("passenger_temp_setting"); }
    public boolean autoConditioning() { return getBoolean("is_auto_conditioning_on"); }
    public int     isFrontDefrosterOn() { return getInteger("is_front_defroster_on"); }
    public boolean isRearDefrosterOn() { return getBoolean("is_rear_defroster_on"); }
    public int     fanStatus() { return getInteger("fan_status"); }

    
    //
    // Override methods
    //
    
    public String toString() {
        return String.format(
            "    Inside Temp: %3.0f\n" +
            "    Outside Temp: %3.0f\n" +
            "    Driver Setpoint: %3.0f\n" +
            "    Passenger Setpoint: %3.0f\n" +
            "    HVAC On: %s\n" +
            "    Front Defroster On: %d\n" +
            "    Rear Defroster On: %s\n" +
            "    Fan Setting: %d\n", 
            Utils.cToF(insideTemp()),
            Utils.cToF(outsideTemp()),
            Utils.cToF(driverTemp()),
            Utils.cToF(passengerTemp()),
            Utils.yesNo(autoConditioning()),
            isFrontDefrosterOn(),
            Utils.yesNo(isRearDefrosterOn()),
            fanStatus()
            );
    }
}
