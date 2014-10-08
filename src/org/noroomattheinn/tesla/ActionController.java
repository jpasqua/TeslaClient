/*
 * ActionController.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 4, 2013
 */

package org.noroomattheinn.tesla;

/**
 * ActionController: Tell the vehicle to perform miscellaneous actions
 * such as honking the horn or flashing the lights.
 * 
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */
public class ActionController extends BaseController {
/*------------------------------------------------------------------------------
 *
 * Constants and Enums
 * 
 *----------------------------------------------------------------------------*/
    private final String HonkCommand;
    private final String FlashLightsCommand;
    private final String WakeupCommand;
    private final String RemoteStartCommand;
    
/*==============================================================================
 * -------                                                               -------
 * -------              Public Interface To This Class                   ------- 
 * -------                                                               -------
 *============================================================================*/
    
    public ActionController(Vehicle v) {
        super(v, "ActionController");
        HonkCommand = Tesla.vehicleCommand(v.getVID(), "honk_horn");
        FlashLightsCommand = Tesla.vehicleCommand(v.getVID(), "flash_lights");
        RemoteStartCommand = Tesla.vehicleCommand(v.getVID(), "remote_start_drive");
        WakeupCommand = Tesla.vehicleSpecific(v.getVID(), "wake_up");
    }

/*------------------------------------------------------------------------------
 *
 * Commands on this controller
 * 
 *----------------------------------------------------------------------------*/
    
    public Result honk() {
        invokeCommand(HonkCommand);
        return new Result(response);
    }

    public Result flashLights() {
        invokeCommand(FlashLightsCommand);
        return new Result(response);
    }

    public Result remoteStart(String password) {
        invokeCommand(RemoteStartCommand, "{'password' : '" + password + "'}");
        return new Result(response);
    }

    public Result wakeUp() {
        invokeCommand(WakeupCommand);
        return new Result(response);
    }

}
