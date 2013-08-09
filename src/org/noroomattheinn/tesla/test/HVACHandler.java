/*
 * HVACHandler.java - Copyright(c) 2013  All Rights Reserved, Joe Pasqua
 * Created: Jul 6, 2013
 */

package org.noroomattheinn.tesla.test;

import org.noroomattheinn.utils.Handler;
import org.noroomattheinn.tesla.HVACController;
import org.noroomattheinn.tesla.HVACState;
import org.noroomattheinn.tesla.Vehicle;
import org.noroomattheinn.utils.CLUtils;

/**
 * HVACHandler
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public class HVACHandler extends TeslaHandler  {
    // Private Constants
    private static final String Description = "Control/View the HVAC System";
    private static final String Name = "hvac";
    
    // Private Instance Variables
    private HVACState state;
    private HVACController controller;
    
    HVACHandler(Vehicle v) {
        super(Name, Description, v);
        state = new HVACState(v);
        controller = new HVACController(v);
        repl.addHandler(new HVACHandler.StartHandler());
        repl.addHandler(new HVACHandler.StopHandler());
        repl.addHandler(new HVACHandler.TemperatureHandler());
        repl.addHandler(new HVACHandler.DisplayHandler());
    }
    
    //
    // Handler classes
    //
    
    class StartHandler extends Handler {
        StartHandler() { super("start", "Start HVAC"); }
        public boolean execute() { controller.startAC(); return true; }
    }
    
    class StopHandler extends Handler {
        StopHandler() { super("stop", "Stop HVAC"); }
        public boolean execute() { controller.stopAC(); return true; }
    }
    
    class DisplayHandler extends Handler {
        DisplayHandler() { super("display", "Display HVAC state", "d"); }
        public boolean execute() {
            if (state.refresh())
                System.out.format("HVAC State:\n%s\n", state);
            else
                System.err.println("Problem communicating with Tesla");
            return true;
        }
    }
    
    class TemperatureHandler extends Handler {
        TemperatureHandler() { super("temp", "Set the temperature", "t"); }
        public boolean execute() {
            float temp = (float)CLUtils.getNumberInRange("Temp (F)", 65.0, 75.0);
            controller.setTempF(temp, temp);
            return true;
        }
    }
}
