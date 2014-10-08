/*
 * DoorController.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 5, 2013
 */

package org.noroomattheinn.tesla;

/**
 * DoorController: Allow locking/unlocking of the doors, opening the charge port,
 * and controlling the panoramic roof.
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public class DoorController extends BaseController {
/*------------------------------------------------------------------------------
 *
 * Constants and Enums
 * 
 *----------------------------------------------------------------------------*/
    public enum PanoCommand {open, comfort, vent, close};

    private final String OpenChargePortCommand;
    private final String UnlockCommand;
    private final String LockCommand;
    private final String SunroofCommand;
    private final String OpenTrunkCommand;
    
    
/*==============================================================================
 * -------                                                               -------
 * -------              Public Interface To This Class                   ------- 
 * -------                                                               -------
 *============================================================================*/
    
    public DoorController(Vehicle v) {
        super(v, "DoorController");
        OpenChargePortCommand = Tesla.vehicleCommand(v.getVID(), "charge_port_door_open");
        UnlockCommand = Tesla.vehicleCommand(v.getVID(), "door_unlock");
        LockCommand = Tesla.vehicleCommand(v.getVID(), "door_lock");
        SunroofCommand = Tesla.vehicleCommand(v.getVID(), "sun_roof_control");
        OpenTrunkCommand = Tesla.vehicleCommand(v.getVID(), "trunk_open");
    }

    
/*------------------------------------------------------------------------------
 *
 * Commands on this controller
 * 
 *----------------------------------------------------------------------------*/
    
    public Result setLockState(boolean locked) {
        invokeCommand(locked ? LockCommand : UnlockCommand);
        return new Result(response);
    }
    
    public Result lockDoors() { return setLockState(true); }

    public Result unlockDoors() { return setLockState(false); }
    
    public Result openChargePort() {
        invokeCommand(OpenChargePortCommand);
        return new Result(response);
    }
    
    public Result openFrunk() { // Requires 6.0 or greater
        invokeCommand(OpenTrunkCommand, "{'whichTrunk' : 'front'}");
        return new Result(response);
    }
    
    public Result openTrunk() { // Requires 6.0 or greater
        invokeCommand(OpenTrunkCommand, "{'whichTrunk' : 'rear'}");
        return new Result(response);
    }
    
    public Result setPano(PanoCommand cmd) {
        String payload = String.format("{'state' : '%s'}", cmd.name());
        invokeCommand(SunroofCommand, payload);
        return new Result(response);
    }
    
    public Result stopPano() {
        invokeCommand(SunroofCommand, "{'state' : 'stop'}");
        return new Result(response);
    }
    
}
