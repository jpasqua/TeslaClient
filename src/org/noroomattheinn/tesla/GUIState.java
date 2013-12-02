/*
 * GUIState.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
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
    
    public State state;
    
    //
    // Constructors
    //
    
    public GUIState(Vehicle v) {
        super(v, Tesla.command(v.getVID(), "gui_settings"));
    }
    
    @Override protected BaseState setState(boolean valid) {
        return (state = valid ? new State(this) : null);
    }
    
    @Override public String getStateName() { return "GUI State"; }
    
    public static class State extends BaseState {
        public String distanceUnits;
        public String temperatureUnits;
        public String chargeRateUnits;
        public boolean use24HrTime;
        public String rangeDisplay;
        
        public State(GUIState gs) {
            distanceUnits = gs.getString("gui_distance_units"); 
            temperatureUnits = gs.getString("gui_temperature_units"); 
            chargeRateUnits = gs.getString("gui_charge_rate_units"); 
            use24HrTime = gs.getBoolean("gui_24_hour_time"); 
            rangeDisplay = gs.getString("gui_range_display"); 
        }
    }
}
