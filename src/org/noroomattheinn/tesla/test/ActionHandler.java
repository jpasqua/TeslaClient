/*
 * ActionHandler.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 6, 2013
 */

package org.noroomattheinn.tesla.test;

import org.noroomattheinn.utils.Handler;
import org.noroomattheinn.tesla.Vehicle;

/**
 * ActionHandler: Implements miscellaneous vehicle actions such as honking
 * and flashing the lights.
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public class ActionHandler extends TeslaHandler  {
    // Constants
    private static final String Description = "Perform Miscellaneous Actions";
    private static final String Name = "actions";
    

    
    //
    // Constructors
    //
    
    ActionHandler(Vehicle v) {
        super(Name, Description, v);
        repl.addHandler(new ActionHandler.Honk());
        repl.addHandler(new ActionHandler.Flash());
        repl.addHandler(new ActionHandler.Wakeup());
    }
    
    //
    // Handler classes
    //
    
    class Honk extends Handler {
        Honk() { super("honk", "Honk the horn", "h"); }
        @Override public boolean execute() { vehicle.honk(); return true; }
    }
    
    class Flash extends Handler {
        Flash() { super("flash", "Flash the lights", "f"); }
        @Override public boolean execute() { vehicle.flashLights(); return true; }
    }
    
    class Wakeup extends Handler {
        Wakeup() { super("wakeup", "Wakeup the car", "w"); }
        @Override public boolean execute() { vehicle.wakeUp(); return true; }
    }
    
}
