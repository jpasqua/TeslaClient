/*
 * ActionHandler.java - Copyright(c) 2013  All Rights Reserved, Joe Pasqua
 * Created: Jul 6, 2013
 */

package org.noroomattheinn.tesla.test;

import org.noroomattheinn.utils.Handler;
import org.noroomattheinn.tesla.ActionController;
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
    
    // Instance Variables
    private ActionController controller;

    
    //
    // Constructors
    //
    
    ActionHandler(Vehicle v) {
        super(Name, Description, v);
        controller = new ActionController(v);
        repl.addHandler(new ActionHandler.Honk());
        repl.addHandler(new ActionHandler.Flash());
        repl.addHandler(new ActionHandler.Wakeup());
    }
    
    //
    // Handler classes
    //
    
    class Honk extends Handler {
        Honk() { super("honk", "Honk the horn", "h"); }
        public boolean execute() { controller.honk(); return true; }
    }
    
    class Flash extends Handler {
        Flash() { super("flash", "Flash the lights", "f"); }
        public boolean execute() { controller.flashLights(); return true; }
    }
    
    class Wakeup extends Handler {
        Wakeup() { super("wakeup", "Wakeup the car", "w"); }
        public boolean execute() { controller.wakeUp(); return true; }
    }
    
}
