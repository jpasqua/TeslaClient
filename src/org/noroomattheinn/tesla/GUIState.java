/*
 * GUIState.java - Copyright(c) 2013  All Rights Reserved, Joe Pasqua
 * Created: Jul 5, 2013
 */

package org.noroomattheinn.tesla;

/**
 * GUIState: Retrieve the parameters that describe the units used in the GUI.
 * For example, it indicates whether speed is to be shown as mi/hr or kph.
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public class GUIState extends APICall {
    
    //
    // Constructors
    //
    
    public GUIState(Vehicle v) {
        super(v, Tesla.command(v.getVID(), "gui_settings"));
    }
    
    //
    // Field Accessor Methods
    //

    public String getStateName() { return "GUI State"; }
    
    public String distanceUnits()    { return getString("gui_distance_units"); }
    public String temperatureUnits() { return getString("gui_temperature_units"); }
    public String chargeRateUnits()  { return getString("gui_charge_rate_units"); }
    public boolean use24HrTime()     { return getBoolean("gui_24_hour_time"); }
    public String rangeDisplay()     { return getString("gui_range_display"); }

}
