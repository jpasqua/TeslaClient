/*
 * ChargeController.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 5, 2013
 */

package org.noroomattheinn.tesla;

/**
 * ChargeController: Control charging parameters and start/stop charging.
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public class ChargeController extends APICall {
    // Instance Variables - These are effectively constants
    private final String startCommand, stopCommand;
    private final String maxRangeCommand, stdRangeCommand;
    private final String chargePctCommand;
    
    //
    // Constructors
    //
    
    public ChargeController(Vehicle v) {
        super(v, "ChargeController");
        startCommand = Tesla.vehicleCommand(v.getVID(), "charge_start");
        stopCommand = Tesla.vehicleCommand(v.getVID(), "charge_stop");
        maxRangeCommand = Tesla.vehicleCommand(v.getVID(), "charge_max_range");
        stdRangeCommand = Tesla.vehicleCommand(v.getVID(), "charge_standard");
        chargePctCommand = Tesla.vehicleCommand(v.getVID(), "set_charge_limit");
    }

    
    //
    // Action Methods
    //
    
    public Result setChargeState(boolean charging) {
        invokeCommand(charging? startCommand : stopCommand);
        return new Result(this);
    }
    
    public Result startCharing() { return setChargeState(true); }

    public Result stopCharing() { return setChargeState(false); }
    
    public Result setChargeRange(boolean max) {
        invokeCommand(max ? maxRangeCommand : stdRangeCommand);
        return new Result(this);
    }
    
    public Result setChargePercent(int percent) {
        if (percent < 1 || percent > 100)
            return new Result(false, "value out of range");
        invokeCommand(chargePctCommand,  String.format("{'percent' : '%d'}", percent));
        return new Result(this);
    }
}
