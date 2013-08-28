/*
 * Interactive.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 6, 2013
 */

package org.noroomattheinn.tesla.test;

import org.noroomattheinn.utils.Handler;
import org.noroomattheinn.utils.REPL;
import java.util.ArrayList;
import java.util.List;
import org.noroomattheinn.tesla.Tesla;
import org.noroomattheinn.tesla.Vehicle;
import org.noroomattheinn.utils.CLUtils;

/**
 * Interactive
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public class Interactive  {

    // Private Constants
    private static final String Name = "Tesla";
    
    // Private Instance Variables
    private Tesla tesla;
    private List<Vehicle> vehicles = new ArrayList<>();
    private Vehicle selectedVehicle = null;
    private boolean addedSubHandlers = false;
    private REPL repl;
    
    //
    // Constructors
    //
    
    public Interactive() {
        this.repl = new REPL(Name);
        tesla = new Tesla();
        
        // The only command available inititally is "login". Only after the
        // user has logged in are the other commands made available
        repl.addHandler(new LoginHandler());
    }

    public boolean execute() { repl.repl(); return true; }
    
    //
    // Private Instance Methods
    //
    
    private void addTeslaHandlers() {
        if (!addedSubHandlers) {
            // If we haven't already added the handlers associated with
            // Tesla commands, add them now
            repl.addHandler(new ChargeHandler(selectedVehicle));
            repl.addHandler(new HVACHandler(selectedVehicle));
            repl.addHandler(new ActionHandler(selectedVehicle));
            repl.addHandler(new DoorHandler(selectedVehicle));
            repl.addHandler(new LocationHandler(selectedVehicle));
            repl.addHandler(new StreamingHandler(selectedVehicle));
            addedSubHandlers = true;
        }
    }

    
    //
    // Main
    //

    public static void main(String[] args) {
        Interactive topLevel = new Interactive();
        topLevel.execute();
    }
    
    
    //
    // Private Utility Classes
    //
    
    private class LoginHandler extends Handler {
        LoginHandler() { super("login", "Connect to Tesla", "l"); }
        public boolean execute() {
            // Try connecting with stored credentials. If that doesn't work
            // then prompt the user for a username / password and try again
            System.out.print("Attempting automatic connection...");
            if (tesla.connect()) {
                System.out.println("done");
            } else {
                System.out.println("Sorry, Credentials required");
                String username = CLUtils.getLine("Username");
                String password = CLUtils.getPassword();
                if (!tesla.connect(username, password, true)) {
                    System.err.println("Unable to connect with those credentials");
                    return true;
                }
            }

            vehicles = tesla.getVehicles();
            // Now that we've got a list of vehicles, we can add a few more Handlers
            repl.addHandler(new SelectHandler());
            repl.addHandler(new ListHandler());
            
            if (vehicles != null && !vehicles.isEmpty()) {
                selectedVehicle = vehicles.get(0);
                addTeslaHandlers();
            }
            
            return true;
        }
    }

    private class SelectHandler extends Handler {
        SelectHandler() { super("select", "Select a vehicle"); }
        public boolean execute() {             
            if (vehicles.size() == 1)
                selectedVehicle = vehicles.get(0);
            else {
                int index =(int)CLUtils.getNumberInRange(
                        "Select vehicle #", 1, vehicles.size());
                selectedVehicle = vehicles.get(index-1);
            }
            // Now that we have a selected vehicle, we can add the other handlers
            addTeslaHandlers();
            
            return true;
        }
    }

    private class ListHandler extends Handler {
        ListHandler() { super("list", "List all Vehicles"); }
        public boolean execute() {
            int index = 1;
            for (Vehicle v : vehicles) {
                System.out.println("Vehicle #" + index++);
                System.out.println(v.toString());
            }
            return true;
        }
    }

}
