/*
 * HVACController.java - Copyright(c) 2013  All Rights Reserved, Joe Pasqua
 * Created: Jul 5, 2013
 */

package org.noroomattheinn.tesla;

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
    private final String tempFormat;
    
    
    //
    // Constructors
    //
    
    public HVACController(Vehicle v) {
        super(v);
        startCommand = Tesla.command(v.getVID(), "auto_conditioning_start");
        stopCommand = Tesla.command(v.getVID(), "auto_conditioning_stop");
        tempFormat = Tesla.command(
                v.getVID(), "set_temps?driver_temp=%3.1f&passenger_temp=%3.1f");
    }

    
    //
    // Action Methods
    //
    
    public Result setAC(boolean on) {
        if (on) return startAC();
        else return stopAC();
    }
    
    public Result startAC() {
        setAndRefresh(startCommand);
        return new Result(this);
    }

    public Result stopAC() {
        setAndRefresh(stopCommand);
        return new Result(this);
    }
    
    public Result setTempC(double driverTemp, double passengerTemp) {
        String command = String.format(tempFormat, driverTemp, passengerTemp);
        setAndRefresh(command);
        return new Result(this);
    }
    
    public Result setTempF(double driverTemp, double passengerTemp) {
        return setTempC(Utils.fToC(driverTemp), Utils.fToC(passengerTemp));
    }
}
