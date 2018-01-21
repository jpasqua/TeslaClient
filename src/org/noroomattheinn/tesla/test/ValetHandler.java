/*
 * DoorHandler.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 6, 2013
 */

package org.noroomattheinn.tesla.test;

import org.noroomattheinn.tesla.Vehicle;
import org.noroomattheinn.tesla.Vehicle.PanoCommand;
import org.noroomattheinn.tesla.VehicleState;
import org.noroomattheinn.utils.CLUtils;
import org.noroomattheinn.utils.Handler;

/**
 * ValetHandler
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public class ValetHandler extends TeslaHandler  {
    // Private Constants
    private static final String Description = "Control/View the Valet Mode State";
    private static final String Name = "valet";

    // Private Instance Variables
    private VehicleState state;



    ValetHandler(Vehicle v) {
        super(Name, Description, v);
        repl.addHandler(new ValetHandler.OnHandler());
        repl.addHandler(new ValetHandler.OffHandler());
        repl.addHandler(new ValetHandler.ClearPinHandler());
    }
    
    //
    // Handler classes
    //
    
    class OnHandler extends Handler {
        OnHandler() { super("on", "Turn on Valet Mode", "v"); }
        @Override public boolean execute() {
            String pin = CLUtils.getLine("Valet PIN");
            vehicle.valet(true, pin); return true;
        }
    }
    
    class OffHandler extends Handler {
        OffHandler() { super("off", "Turn off Valet Mode", "o"); }
        @Override public boolean execute() { vehicle.valet(false); return true; }
    }

    class ClearPinHandler extends Handler {
        ClearPinHandler() { super("clear", "Clear Valet PIN", "c"); }
        @Override public boolean execute() { vehicle.clearValetPin(); return true; }
    }
}
