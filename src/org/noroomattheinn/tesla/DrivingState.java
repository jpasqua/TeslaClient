/*
 * DrivingState.java - Copyright(c) 2013  All Rights Reserved, Joe Pasqua
 * Created: Jul 5, 2013
 */

package org.noroomattheinn.tesla;

/**
 * DrivingState: Retrieve the driving state of the vehicle. This includes
 * the location and heading.
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public class DrivingState extends APICall {
    
    //
    // Constructors
    //
    
    public DrivingState(Vehicle v) {
        super(v, Tesla.command(v.getVID(), "drive_state"));
    }
    

    //
    // Field Accessor Methods
    //
    
    public String getStateName() { return "Driving State"; }
    
    public double latitude()  { return getDouble("latitude"); }
    public double longitude() { return getDouble("longitude"); }
    public int    heading()   { return getInteger("heading"); }
    public int    gpsAsOf()   { return getInteger("gps_as_of"); }
    
    // The following calls aren't well defined in terms of what type and values 
    // they return. We're leaving them as String for now
    public String shiftState() { return getString("shift_state"); }
    public String speed()      { return getString("speed"); }
}
