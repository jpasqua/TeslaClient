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
public class ActionController extends APICall {
    // Instance Variables - These are effectively constants
    private final String honkCommand;
    private final String flashLightsCommand;
    private final String wakeupCommand;
    private final String remoteStartCommand;
    
    //
    // Constructors
    //
    
    public ActionController(Vehicle v) {
        super(v, "ActionController");
        honkCommand = Tesla.vehicleCommand(v.getVID(), "honk_horn");
        flashLightsCommand = Tesla.vehicleCommand(v.getVID(), "flash_lights");
        remoteStartCommand = Tesla.vehicleCommand(v.getVID(), "remote_start_drive");
        wakeupCommand = Tesla.vehicleSpecific(v.getVID(), "wake_up");
    }

    
    //
    // Action Methods
    //
    
    public Result honk() {
        invokeCommand(honkCommand);
        return new Result(this);
    }

    public Result flashLights() {
        invokeCommand(flashLightsCommand);
        return new Result(this);
    }

    public Result remoteStart(String password) {
        invokeCommand(remoteStartCommand, "{'password' : '" + password + "'}");
        return new Result(this);
    }

    public Result wakeUp() {
        invokeCommand(wakeupCommand);
        return new Result(this);
    }

}
