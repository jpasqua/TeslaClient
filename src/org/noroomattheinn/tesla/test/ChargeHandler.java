/*
 * ChargeHandler.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 6, 2013
 */

package org.noroomattheinn.tesla.test;

import org.noroomattheinn.utils.Handler;
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

    ChargeHandler(Vehicle v) {
        super(Name, Description, v);
        repl.addHandler(new StartHandler());
        repl.addHandler(new StopHandler());
        repl.addHandler(new PercentHandler());
        repl.addHandler(new TargetHandler());
        repl.addHandler(new DisplayHandler());
    }
    
    //
    // Handler classes
    //
    
    class StartHandler extends Handler {
        StartHandler() { super("start", "Start Charging"); }
        @Override public boolean execute() { vehicle.startCharing(); return true; }
    }
    
    class StopHandler extends Handler {
        StopHandler() { super("stop", "Stop Charging"); }
        @Override public boolean execute() { vehicle.stopCharing(); return true; }
    }
    
    class DisplayHandler extends Handler {
        DisplayHandler() { super("display", "Display Charge State", "d"); }
        @Override public boolean execute() {
            state = vehicle.queryCharge();
            if (state.valid)
                System.out.format("Charge State:\n%s\n", state);
            else
                System.err.println("Problem communicating with Tesla");            
            return true;
        }
    }
    
    class PercentHandler extends Handler {
        PercentHandler() { super("percent", "Set Charge Percent", "%"); }
        @Override public boolean execute() {
            int percent = (int)CLUtils.getNumberInRange("Charge Percent", 0, 100);
            vehicle.setChargePercent(percent);
            return true;
        }
    }
    
    class TargetHandler extends Handler {
        TargetHandler() { super("target", "Set the charge target (max or std)"); }
        @Override public boolean execute() {
            String[] options = {"max", "std"};
            String target = CLUtils.chooseOption("Target charge", options);
            vehicle.setChargeRange(target.equalsIgnoreCase("max"));
            return true;
        }
    }
}
