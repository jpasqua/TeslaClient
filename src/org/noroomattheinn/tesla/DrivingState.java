/*
 * DrivingState.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 5, 2013
 */

package org.noroomattheinn.tesla;

/**
 * DrivingState: Retrieve the driving state of the vehicle. This includes
 * the location and heading.
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public class DrivingState extends StateAPI {
    
    public State state;
    
    //
    // Constructors
    //
    
    public DrivingState(Vehicle v) {
        super(v, Tesla.vehicleData(v.getVID(), "drive_state"), "Drive State");
    }
    
    @Override protected void setState(boolean valid) {
        state = valid ? new State(this) : null;
    }
    
    public static class State extends BaseState {
        public double   latitude;
        public double   longitude;
        public int      heading;
        public int      gpsAsOf;
    
        // The following calls aren't well defined in terms of what type and values 
        // they return. We're leaving them as String for now
        public String   shiftState;
        public String   speed;
        
        public State(DrivingState ds) {
            latitude = ds.getDouble("latitude"); 
            longitude = ds.getDouble("longitude"); 
            heading = ds.getInteger("heading"); 
            gpsAsOf = ds.getInteger("gps_as_of"); 
            shiftState = ds.getString("shift_state"); 
            speed = ds.getString("speed"); 
        }
    }
}
