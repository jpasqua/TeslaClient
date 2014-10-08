/*
 * HVACHandler.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
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
        @Override public boolean execute() { controller.startAC(); return true; }
    }
    
    class StopHandler extends Handler {
        StopHandler() { super("stop", "Stop HVAC"); }
        @Override public boolean execute() { controller.stopAC(); return true; }
    }
    
    class DisplayHandler extends Handler {
        DisplayHandler() { super("display", "Display HVAC state", "d"); }
        @Override public boolean execute() {
            state = vehicle.queryHVAC();
            if (state.valid)
                System.out.format("HVAC State:\n%s\n", state);
            else
                System.err.println("Problem communicating with Tesla");
            return true;
        }
    }
    
    class TemperatureHandler extends Handler {
        TemperatureHandler() { super("temp", "Set the temperature", "t"); }
        @Override public boolean execute() {
            //float temp = (float)CLUtils.getNumberInRange("Temp (F)", 65.0, 75.0);
            //controller.setTempF(temp, temp);
            float temp = (float)CLUtils.getNumberInRange("Temp (C)", 17.0, 22.0);
            controller.setTempC(temp, temp);
            return true;
        }
    }
}
