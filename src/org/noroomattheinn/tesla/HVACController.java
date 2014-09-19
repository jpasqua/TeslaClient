/*
 * HVACController.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 5, 2013
 */

package org.noroomattheinn.tesla;

import java.util.Locale;
import org.noroomattheinn.utils.Utils;

/**
 * HVACController: Starts and stops the HVAC and allows the desired temperature
 * to be set for the driver and passenger. Note that starting the HVAC may cause
 * heating or cooling depending on the temperature set point.
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public class HVACController extends APICall {
    // Instance Variables - These are effectively constants
    private final String startCommand;
    private final String stopCommand;
    private final String tempsCommand;
    
    //
    // Constructors
    //
    
    public HVACController(Vehicle v) {
        super(v, "HVAC Controller");
        startCommand = Tesla.vehicleCommand(v.getVID(), "auto_conditioning_start");
        stopCommand = Tesla.vehicleCommand(v.getVID(), "auto_conditioning_stop");
        tempsCommand = Tesla.vehicleCommand(v.getVID(), "set_temps");
    }

    
    //
    // Action Methods
    //
    
    public Result setAC(boolean on) {
        if (on) return startAC();
        else return stopAC();
    }
    
    public Result startAC() {
        invokeCommand(startCommand);
        return new Result(this);
    }

    public Result stopAC() {
        invokeCommand(stopCommand);
        return new Result(this);
    }
    
    public Result setTempC(double driverTemp, double passengerTemp) {
        String tempsPayload = String.format(Locale.US,
                "{'driver_temp' : '%3.1f', 'passenger_temp' : '%3.1f'}",
                driverTemp, passengerTemp);
        invokeCommand(tempsCommand, tempsPayload);
        return new Result(this);
    }
    
    public Result setTempF(double driverTemp, double passengerTemp) {
        return setTempC(Utils.fToC(driverTemp), Utils.fToC(passengerTemp));
    }
}
