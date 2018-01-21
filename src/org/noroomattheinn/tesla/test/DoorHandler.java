/*
 * DoorHandler.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 6, 2013
 */

package org.noroomattheinn.tesla.test;

import org.noroomattheinn.utils.Handler;
import org.noroomattheinn.tesla.VehicleState;
import org.noroomattheinn.tesla.Vehicle;
import org.noroomattheinn.tesla.Vehicle.PanoCommand;
import org.noroomattheinn.utils.CLUtils;

/**
 * DoorHandler
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public class DoorHandler extends TeslaHandler  {
    // Private Constants
    private static final String Description = "Control/View the Door State";
    private static final String Name = "doors";
    
    // Private Instance Variables
    private VehicleState state;


    
    DoorHandler(Vehicle v) {
        super(Name, Description, v);
        repl.addHandler(new DoorHandler.LockHandler());
        repl.addHandler(new DoorHandler.UnlockHandler());
        repl.addHandler(new DoorHandler.PortHandler());
        repl.addHandler(new DoorHandler.PanoHandler());
        repl.addHandler(new DoorHandler.PanoPercentHandler());
        repl.addHandler(new DoorHandler.DisplayHandler());
    }
    
    //
    // Handler classes
    //
    
    class LockHandler extends Handler {
        LockHandler() { super("lock", "Lock the doors", "l"); }
        @Override public boolean execute() { vehicle.lockDoors(); return true; }
    }
    
    class UnlockHandler extends Handler {
        UnlockHandler() { super("unlock", "Unlock the doors", "u"); }
        @Override public boolean execute() { vehicle.unlockDoors(); return true; }
    }
    
    class PortHandler extends Handler {
        PortHandler() { super("port", "Open the charge port", "o"); }
        @Override public boolean execute() { vehicle.openChargePort(); return true; }
    }
    
    class DisplayHandler extends Handler {
        DisplayHandler() { super("display", "Display Door State", "d"); }
        @Override public boolean execute() {
            state = vehicle.queryVehicle();
            if (state.valid)
                System.out.format("Door State:\n%s\n", state);
            else
                System.err.println("Problem communicating with Tesla");
            return true;
        }
    }
    
    class PanoHandler extends Handler {
        PanoHandler() { super("pano", "Control the roof", "p"); }
        @Override
        public boolean execute() {
            PanoCommand cmd = PanoCommand.valueOf(CLUtils.chooseOption(
                    "Pano Command", PanoCommand.values()));
            vehicle.setPano(cmd);
            return true;
        }
    }

    class PanoPercentHandler extends Handler {
        PanoPercentHandler() { super("pano_percent", "Control the roof precisely", "r"); }
        @Override public boolean execute() {
            int percent = (int)CLUtils.getNumberInRange("Roof Percent", 0, 100);
            vehicle.setPanoPercent(percent);
            return true;
        }
    }
}
