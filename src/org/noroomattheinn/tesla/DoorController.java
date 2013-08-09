/*
 * DoorController.java - Copyright(c) 2013  All Rights Reserved, Joe Pasqua
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
    private final String sunroofFormat;
    
    
    //
    // Constructors
    //
    
    public DoorController(Vehicle v) {
        super(v);
        openChargePortCommand = Tesla.command(v.getVID(), "charge_port_door_open");
        unlockCommand = Tesla.command(v.getVID(), "door_unlock");
        lockCommand = Tesla.command(v.getVID(), "door_lock");
        sunroofFormat = Tesla.command(v.getVID(), "sun_roof_control?state=%s");
    }

    
    //
    // Action Methods
    //
    
    public Result setLockState(boolean locked) {
        setAndRefresh(locked ? lockCommand : unlockCommand);
        return new Result(this);
    }
    
    public Result lockDoors() { return setLockState(true); }

    public Result unlockDoors() { return setLockState(false); }
    
    public Result openChargePort() {
        setAndRefresh(openChargePortCommand);
        return new Result(this);
    }
    
    public Result setPano(PanoCommand cmd) {
        String command = String.format(sunroofFormat, cmd.name());
        setAndRefresh(command);
        return new Result(this);
    }
    
    
    //
    // Nested Classes
    //
    
    public enum PanoCommand {open, comfort, vent, close};

}
