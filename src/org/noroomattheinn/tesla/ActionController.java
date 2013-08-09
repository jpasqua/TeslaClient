/*
 * ActionController.java - Copyright(c) 2013  All Rights Reserved, Joe Pasqua
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
    private final String honkCommand;         // honk_horn
    private final String flashLightsCommand;  // flash_lights
    private final String wakeupCommand;       // wake_up
    
    
    //
    // Constructors
    //
    
    public ActionController(Vehicle v) {
        super(v);
        honkCommand = Tesla.command(v.getVID(), "honk_horn");
        flashLightsCommand = Tesla.command(v.getVID(), "flash_lights");
        wakeupCommand = Tesla.command(v.getVID(), "wake_up");
    }

    
    //
    // Action Methods
    //
    
    public Result honk() {
        setAndRefresh(honkCommand);
        return new Result(this);
    }

    public Result flashLights() {
        setAndRefresh(flashLightsCommand);
        return new Result(this);
    }

    public Result wakeUp() {
        setAndRefresh(wakeupCommand);
        return new Result(this);
    }

}
