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

public class DoorController extends APICall {
    // Instance Variables - These are effectively constants
    private final String openChargePortCommand;
    private final String unlockCommand;
    private final String lockCommand;
    private final String sunroofCommand;
    private final String openTrunkCommand;
    
    
    //
    // Constructors
    //
    
    public DoorController(Vehicle v) {
        super(v, "DoorController");
        openChargePortCommand = Tesla.vehicleCommand(v.getVID(), "charge_port_door_open");
        unlockCommand = Tesla.vehicleCommand(v.getVID(), "door_unlock");
        lockCommand = Tesla.vehicleCommand(v.getVID(), "door_lock");
        sunroofCommand = Tesla.vehicleCommand(v.getVID(), "sun_roof_control");
        openTrunkCommand = Tesla.vehicleCommand(v.getVID(), "trunk_open");
    }

    
    //
    // Action Methods
    //
    
    public Result setLockState(boolean locked) {
        invokeCommand(locked ? lockCommand : unlockCommand);
        return new Result(this);
    }
    
    public Result lockDoors() { return setLockState(true); }

    public Result unlockDoors() { return setLockState(false); }
    
    public Result openChargePort() {
        invokeCommand(openChargePortCommand);
        return new Result(this);
    }
    
    public Result openFrunk() { // Requires 6.0 or greater
        invokeCommand(openTrunkCommand, "{'whichTrunk' : 'front'}");
        return new Result(this);
    }
    
    public Result openTrunk() { // Requires 6.0 or greater
        invokeCommand(openTrunkCommand, "{'whichTrunk' : 'rear'}");
        return new Result(this);
    }
    
    public Result setPano(PanoCommand cmd) {
        String payload = String.format("{'state' : '%s'}", cmd.name());
        invokeCommand(sunroofCommand, payload);
        return new Result(this);
    }
    
    public Result stopPano() {
        invokeCommand(sunroofCommand, "{'state' : 'stop'}");
        return new Result(this);
    }
    
    //
    // Nested Classes
    //
    
    public enum PanoCommand {open, comfort, vent, close};

}
