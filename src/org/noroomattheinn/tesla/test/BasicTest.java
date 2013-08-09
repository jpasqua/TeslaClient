/*
 * BasicTest.java - Copyright(c) 2013  All Rights Reserved, Joe Pasqua
 * Created: Jul 5, 2013
 */

package org.noroomattheinn.tesla.test;

import java.util.List;
import java.util.logging.Level;
import org.noroomattheinn.tesla.ChargeState;
import org.noroomattheinn.tesla.DoorState;
import org.noroomattheinn.tesla.DrivingState;
import org.noroomattheinn.tesla.GUIState;
import org.noroomattheinn.tesla.HVACState;
import org.noroomattheinn.tesla.StreamingState;
import org.noroomattheinn.tesla.Tesla;
import org.noroomattheinn.tesla.Vehicle;
import org.noroomattheinn.tesla.VehicleState;

/**
 * BasicTest
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public class BasicTest {

     /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {            
        Tesla t = new Tesla();
                
        if (args.length == 2) {
            if (!t.connect(args[0], args[1])) {
                System.err.println("Unable to connect with supplied u/p");
                System.exit(1);
            }
        } else {
            if (!t.connect()) {
                System.err.println("Unable to connect with stored credentials");
                System.exit(1);
            }
        }
        
        
        List<Vehicle> vehicles = t.getVehicles();
        Tesla.logger.log(Level.INFO, "Number of vehicles: {0}", vehicles.size());
        
        for (Vehicle vehicle : vehicles) {
            StreamingState ss = new StreamingState(vehicle);
            while (ss.refresh()) {
                System.out.println(ss);
            }
            System.out.format("%s\n", vehicle);
            System.out.format("Mobile Enabled: %s\n", vehicle.mobileEnabled());
            VehicleState state = new VehicleState(vehicle); state.refresh();
            System.out.format("SW Version: %s\n", state.version());
            DoorState ds = new DoorState(vehicle); ds.refresh();
            System.out.format("Pano Percent: %d\n", ds.panoPercent());
            System.out.format("Pano State: %s\n", ds.panoState());
            System.out.format("DoorState: %s\n", ds);
            
//            DoorController doorController = new DoorController(vehicle);
//            doorController.setPano(DoorController.PanoCommand.vent);
//            ds.refresh();
//            System.out.format("Pano Percent: %d\n", ds.panoPercent());
//            System.out.format("Pano State: %s\n", ds.panoState());
//            System.out.format("DoorState: %s\n", ds);

            GUIState gui = new GUIState(vehicle); gui.refresh();
            System.out.format("Charge Rate Units: %s\n", gui.chargeRateUnits());
            System.out.format("GUIState dump: %s\n", gui.toString());
            HVACState hvac = new HVACState(vehicle); hvac.refresh();
            System.out.format("Fan Status: %d\n", hvac.fanStatus());
            ChargeState cs = new ChargeState(vehicle); cs.refresh();
            System.out.format("FastChargerPresent: %s\n", cs.fastChargerPresent());
            System.out.format("ChargeState dump: %s\n", cs.toString());
            DrivingState drs = new DrivingState(vehicle); drs.refresh();
            System.out.format("DrivingState dump: %s\n", drs.toString());

            
//ActionController actions = new ActionController(vehicle);
            //actions.honk();
        }
    }
}
