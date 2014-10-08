/*
 * ChargeController.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 5, 2013
 */

package org.noroomattheinn.tesla;

import us.monoid.json.JSONException;

/**
 * ChargeController: Control charging parameters and start/stop charging.
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public class ChargeController extends BaseController {
/*------------------------------------------------------------------------------
 *
 * Constants and Enums
 * 
 *----------------------------------------------------------------------------*/
    private final String StartCommand, StopCommand;
    private final String MaxRangeCommand, StdRangeCommand;
    private final String ChargePctCommand;
    
/*==============================================================================
 * -------                                                               -------
 * -------              Public Interface To This Class                   ------- 
 * -------                                                               -------
 *============================================================================*/
    
    public ChargeController(Vehicle v) {
        super(v, "ChargeController");
        StartCommand = Tesla.vehicleCommand(v.getVID(), "charge_start");
        StopCommand = Tesla.vehicleCommand(v.getVID(), "charge_stop");
        MaxRangeCommand = Tesla.vehicleCommand(v.getVID(), "charge_max_range");
        StdRangeCommand = Tesla.vehicleCommand(v.getVID(), "charge_standard");
        ChargePctCommand = Tesla.vehicleCommand(v.getVID(), "set_charge_limit");
    }

    
/*------------------------------------------------------------------------------
 *
 * Commands on this controller
 * 
 *----------------------------------------------------------------------------*/
    
    public Result setChargeState(boolean charging) {
        invokeCommand(charging? StartCommand : StopCommand);
        return new Result(response);
    }
    
    public Result startCharing() { return setChargeState(true); }

    public Result stopCharing() { return setChargeState(false); }
    
    public Result setChargeRange(boolean max) {
        invokeCommand(max ? MaxRangeCommand : StdRangeCommand);
        return new Result(response);
    }
    
    public Result setChargePercent(int percent) {
        if (percent < 1 || percent > 100)
            return new Result(false, "value out of range");
        invokeCommand(ChargePctCommand,  String.format("{'percent' : '%d'}", percent));
        if (response.optString("reason").equals("already_set")) {
            try {
                response.put("result", true);
            } catch (JSONException e) {
                Tesla.logger.severe("Can't Happen!");
            }
        }
        return new Result(response);
    }
}
