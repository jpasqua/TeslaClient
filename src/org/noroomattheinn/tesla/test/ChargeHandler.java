/*
 * ChargeHandler.java - Copyright(c) 2013  All Rights Reserved, Joe Pasqua
 * Created: Jul 6, 2013
 */

package org.noroomattheinn.tesla.test;

import org.noroomattheinn.utils.Handler;
import org.noroomattheinn.tesla.ChargeController;
import org.noroomattheinn.tesla.ChargeState;
import org.noroomattheinn.tesla.Vehicle;
import org.noroomattheinn.utils.CLUtils;

/**
 * ChargeHandler
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public class ChargeHandler extends TeslaHandler  {
    // Private Constants
    private static final String Description = "Control/View the Charging System";
    private static final String Name = "charge";
    
    // Private Instance Variables
    private ChargeState state;
    private ChargeController controller;

    ChargeHandler(Vehicle v) {
        super(Name, Description, v);
        state = new ChargeState(vehicle);
        controller = new ChargeController(vehicle);
        repl.addHandler(new StartHandler());
        repl.addHandler(new StopHandler());
        repl.addHandler(new TargetHandler());
        repl.addHandler(new DisplayHandler());
    }
    
    //
    // Handler classes
    //
    
    class StartHandler extends Handler {
        StartHandler() { super("start", "Start Charging"); }
        public boolean execute() { controller.startCharing(); return true; }
    }
    
    class StopHandler extends Handler {
        StopHandler() { super("stop", "Stop Charging"); }
        public boolean execute() { controller.stopCharing(); return true; }
    }
    
    class DisplayHandler extends Handler {
        DisplayHandler() { super("display", "Display Charge State", "d"); }
        public boolean execute() {
            if (state.refresh())
                System.out.format("Charge State:\n%s\n", state);
            else
                System.err.println("Problem communicating with Tesla");            
            return true;
        }
    }
    
    class TargetHandler extends Handler {
        TargetHandler() { super("target", "Set the charge target (max or std)"); }
        public boolean execute() {
            String[] options = {"max", "std"};
            String target = CLUtils.chooseOption("Target charge", options);
            controller.setChargeRange(target.equalsIgnoreCase("max"));
            return true;
        }
    }
}
