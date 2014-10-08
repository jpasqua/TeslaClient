/*
 * GUIState.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 5, 2013
 */

package org.noroomattheinn.tesla;

import us.monoid.json.JSONObject;

/**
 * GUIState: Stores the parameters that describe the units used in the GUI.
 * For example, it indicates whether speed is to be shown as mi/hr or kph.
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public class GUIState extends BaseState {
/*------------------------------------------------------------------------------
 *
 * Public State
 * 
 *----------------------------------------------------------------------------*/
    public final String distanceUnits;
    public final String temperatureUnits;
    public final String chargeRateUnits;
    public final boolean use24HrTime;
    public final String rangeDisplay;
    
/*==============================================================================
 * -------                                                               -------
 * -------              Public Interface To This Class                   ------- 
 * -------                                                               -------
 *============================================================================*/
    
    public GUIState(JSONObject source) {
        super(source);
        distanceUnits = source.optString("gui_distance_units"); 
        temperatureUnits = source.optString("gui_temperature_units"); 
        chargeRateUnits = source.optString("gui_charge_rate_units"); 
        use24HrTime = source.optBoolean("gui_24_hour_time"); 
        rangeDisplay = source.optString("gui_range_display"); 
    }
    
    @Override public String toString() {
        return String.format(
            "    Distance Units: %s\n" +
            "    Temperature Units: %s\n" +
            "    Charge Rate Units: %s\n" +
            "    Use 24 Hour Time: %b\n" +
            "    Range Display: %s\n", 
            distanceUnits, temperatureUnits, chargeRateUnits,
            use24HrTime, rangeDisplay);
    }
}
