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

public class HVACController extends BaseController {
/*------------------------------------------------------------------------------
 *
 * Constants and Enums
 * 
 *----------------------------------------------------------------------------*/
    private final String StartCommand;
    private final String StopCommand;
    private final String TempsCommand;
    
/*==============================================================================
 * -------                                                               -------
 * -------              Public Interface To This Class                   ------- 
 * -------                                                               -------
 *============================================================================*/
    
    public HVACController(Vehicle v) {
        super(v, "HVAC Controller");
        StartCommand = Tesla.vehicleCommand(v.getVID(), "auto_conditioning_start");
        StopCommand = Tesla.vehicleCommand(v.getVID(), "auto_conditioning_stop");
        TempsCommand = Tesla.vehicleCommand(v.getVID(), "set_temps");
    }


/*------------------------------------------------------------------------------
 *
 * Commands on this controller
 * 
 *----------------------------------------------------------------------------*/
    
    public Result setAC(boolean on) {
        if (on) return startAC();
        else return stopAC();
    }
    
    public Result startAC() {
        invokeCommand(StartCommand);
        return new Result(response);
    }

    public Result stopAC() {
        invokeCommand(StopCommand);
        return new Result(response);
    }
    
    public Result setTempC(double driverTemp, double passengerTemp) {
        String tempsPayload = String.format(Locale.US,
                "{'driver_temp' : '%3.1f', 'passenger_temp' : '%3.1f'}",
                driverTemp, passengerTemp);
        invokeCommand(TempsCommand, tempsPayload);
        return new Result(response);
    }
    
    public Result setTempF(double driverTemp, double passengerTemp) {
        return setTempC(Utils.fToC(driverTemp), Utils.fToC(passengerTemp));
    }
}
