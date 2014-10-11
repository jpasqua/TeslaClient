/*
 * BasicTest.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 5, 2013
 */

package org.noroomattheinn.tesla.test;

import java.util.List;
import java.util.logging.Level;
import org.noroomattheinn.tesla.ChargeState;
import org.noroomattheinn.tesla.VehicleState;
import org.noroomattheinn.tesla.DriveState;
import org.noroomattheinn.tesla.GUIState;
import org.noroomattheinn.tesla.HVACState;
import org.noroomattheinn.tesla.StreamState;
import org.noroomattheinn.tesla.Streamer;
import org.noroomattheinn.tesla.Tesla;
import org.noroomattheinn.tesla.Vehicle;
//import org.noroomattheinn.utils.Utils;

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
        } else if (args.length == 3) {  // -t username token
            if (!t.connectWithToken(args[0], args[1])) {
                System.err.println("Unable to connect with stored credentials");
                System.exit(1);
            }
        }
        
        
        List<Vehicle> vehicles = t.getVehicles();
        Tesla.logger.log(Level.INFO, "Number of vehicles: {0}", vehicles.size());

        for (Vehicle vehicle : vehicles) {
            System.out.format("%s\n", vehicle);
            System.out.format("Mobile Enabled: %s\n", vehicle.mobileEnabled());
            
            Streamer streamer = vehicle.getStreamer();
            StreamState ss = streamer.refresh();
            if (ss != null) { System.out.println(ss); }
//            while ((ss = streamer.refreshFromStream()) != null) {
//                System.out.println(ss);
//                Utils.sleep(500);
//            }
            VehicleState vs = vehicle.queryVehicle();
            if (vs.valid) {
                try {
                    System.out.println(vs.rawState.toString(4));
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
                System.out.format("SW Version: %s\n", vs.version);
                System.out.format("Pano Percent: %d\n", vs.panoPercent);
                System.out.format("Pano State: %s\n", vs.panoState);
                System.out.format("Vehicle State: %s\n", vs);
            }
            
            vehicle.setPano(Vehicle.PanoCommand.vent);
            vs = vehicle.queryVehicle();
            if (vs.valid) {
                System.out.format("Pano Percent: %d\n", vs.panoPercent);
                System.out.format("Pano State: %s\n", vs.panoState);
            }

            GUIState gui = vehicle.queryGUI();
            if (gui.valid) {
                System.out.format("Charge Rate Units: %s\n", gui.chargeRateUnits);
                System.out.format("GUIState: %s\n", gui.toString());        
            }
            
            HVACState hvac = vehicle.queryHVAC();
            if (hvac.valid) {
                System.out.format("Fan Status: %d\n", hvac.fanStatus);        
            }
            
            ChargeState cs = vehicle.queryCharge();
            if (cs.valid) {
                System.out.format("FastChargerPresent: %s\n", cs.fastChargerPresent);
                System.out.format(
                        "SOC Limit: %d, Min %d, Max: %d, Std: %d\n",
                        cs.chargeLimitSOC, cs.chargeLimitSOCMin,
                        cs.chargeLimitSOCMax, cs.chargeLimitSOCStd);
                System.out.format("ChargeState: %s\n", cs.toString());
            }
            
            DriveState driveState = vehicle.queryDrive();
            if (driveState.valid) {
                System.out.format("DriveState: %s\n", driveState.toString());
            }

            
            //ActionController actions = new ActionController(vehicle);
            //actions.honk();
        }
    }
}
