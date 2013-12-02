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
import org.noroomattheinn.tesla.DrivingState;
import org.noroomattheinn.tesla.GUIState;
import org.noroomattheinn.tesla.HVACState;
import org.noroomattheinn.tesla.SnapshotState;
import org.noroomattheinn.tesla.Tesla;
import org.noroomattheinn.tesla.Vehicle;
import org.noroomattheinn.utils.Utils;

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
            if (!t.connect(args[0], args[1], true)) {
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
            SnapshotState ss = new SnapshotState(vehicle);
            ss.refresh(); System.out.println(ss);
            while (ss.refreshFromStream()) {
                System.out.println(ss);
                Utils.sleep(500);
            }
            System.out.format("%s\n", vehicle);
            System.out.format("Mobile Enabled: %s\n", vehicle.mobileEnabled());
            VehicleState vs = new VehicleState(vehicle); vs.refresh();
            System.out.format("SW Version: %s\n", vs.state.version);
            System.out.format("Pano Percent: %d\n", vs.state.panoPercent);
            System.out.format("Pano State: %s\n", vs.state.panoState);
            System.out.format("DoorState: %s\n", vs);
            
//            DoorController doorController = new DoorController(vehicle);
//            doorController.setPano(DoorController.PanoCommand.vent);
//            ds.refresh();
//            System.out.format("Pano Percent: %d\n", ds.panoPercent());
//            System.out.format("Pano State: %s\n", ds.panoState());
//            System.out.format("VehicleState: %s\n", ds);

            GUIState gui = new GUIState(vehicle); gui.refresh();
            System.out.format("Charge Rate Units: %s\n", gui.state.chargeRateUnits);
            System.out.format("GUIState dump: %s\n", gui.toString());
            HVACState hvac = new HVACState(vehicle); hvac.refresh();
            System.out.format("Fan Status: %d\n", hvac.state.fanStatus);
            ChargeState cs = new ChargeState(vehicle); cs.refresh();
            System.out.format("FastChargerPresent: %s\n", cs.state.fastChargerPresent);
            System.out.format(
                    "SOC Limit: %d, Min %d, Max: %d, Std: %d\n",
                    cs.state.chargeLimitSOC, cs.state.chargeLimitSOCMin,
                    cs.state.chargeLimitSOCMax, cs.state.chargeLimitSOCStd);
            System.out.format("ChargeState dump: %s\n", cs.toString());
            DrivingState drs = new DrivingState(vehicle); drs.refresh();
            System.out.format("DrivingState dump: %s\n", drs.toString());

            
            //ActionController actions = new ActionController(vehicle);
            //actions.honk();
        }
    }
}
