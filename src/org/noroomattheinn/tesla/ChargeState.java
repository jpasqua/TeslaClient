/*
 * ChargeState.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 5, 2013
 */

package org.noroomattheinn.tesla;

import org.noroomattheinn.utils.Utils;
import us.monoid.json.JSONObject;

/**
 * ChargeState: Retrieve the charging state of the vehicle.
 * NOTE: A call to refresh MUST be made before accessing the content of the
 * state. Future calls to refresh may be made to get updated versions of the
 * state.
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public class ChargeState extends BaseState {
/*------------------------------------------------------------------------------
 *
 * Constants and Enums
 * 
 *----------------------------------------------------------------------------*/
    public enum Status {Complete, Charging, Disconnected, Stopped, NoPower, Starting, Unknown};
    
/*------------------------------------------------------------------------------
 *
 * Public State
 * 
 *----------------------------------------------------------------------------*/
    public final boolean  chargeToMaxRange;
    public final int      maxRangeCharges;
    public final double   range;
    public final double   estimatedRange;
    public final double   idealRange;
    public final int      batteryPercent;
    public final double   batteryCurrent;
    public final int      chargerVoltage;
    public final double   timeToFullCharge;
    public final double   chargeRate;
    public final boolean  chargePortOpen;
    public final boolean  scheduledChargePending;
    public final long     scheduledStart;
    public final int      chargerPilotCurrent;
    public final int      chargerActualCurrent;
    public final boolean  fastChargerPresent;
    public final int      chargerPower;
    public final ChargeState.Status   chargingState;

    public final boolean  batteryHeaterOn;
    public final boolean  notEnoughPowerToHeat;
    public final String   fastChargerType;
    public final int      usableBatteryLevel;
    public final double   energyAdded;
    public final double   ratedMilesAdded;
    public final double   idealMilesAdded;
    public final boolean  chargeEnableRequest;

    // The following fields aren't well defined in terms of what type and values 
    // they return. We're leaving them as String for now
    public final String   chargeStartingRange;
    public final String   chargeStartingSOC;
    public final String   userChargeEnableRequest;

    public final int      chargeLimitSOC;
    public final int      chargeLimitSOCMax;
    public final int      chargeLimitSOCMin;
    public final int      chargeLimitSOCStd;

    public final boolean  euVehicle;
    public final int      chargerPhases;
    
/*==============================================================================
 * -------                                                               -------
 * -------              Public Interface To This Class                   ------- 
 * -------                                                               -------
 *============================================================================*/
    
    public ChargeState() { this(emptyJSONObj); }
    
    public ChargeState(JSONObject source) {
        super(source);
        chargeToMaxRange =  source.optBoolean("charge_to_max_range"); 
        maxRangeCharges =  source.optInt("max_range_charge_counter"); 
        range =  source.optDouble("battery_range"); 
        estimatedRange =  source.optDouble("est_battery_range"); 
        idealRange =  source.optDouble("ideal_battery_range"); 
        batteryPercent =  source.optInt("battery_level"); 
        batteryCurrent =  source.optDouble("battery_current"); 
        chargerVoltage =  source.optInt("charger_voltage"); 
        timeToFullCharge =  source.optDouble("time_to_full_charge"); 
        chargeRate =  source.optDouble("charge_rate"); 
        chargePortOpen =  source.optBoolean("charge_port_door_open"); 
        scheduledChargePending = source.optBoolean("scheduled_charging_pending"); 
        scheduledStart =  source.optLong("scheduled_charging_start_time");
        chargerPilotCurrent =  source.optInt("charger_pilot_current", -1);
        chargerActualCurrent =  source.optInt("charger_actual_current"); 
        fastChargerPresent =  source.optBoolean("fast_charger_present"); 
        chargerPower =  source.optInt("charger_power"); 
        chargingState =  Utils.stringToEnum(ChargeState.Status.class, source.optString("charging_state"));
        if (chargingState == ChargeState.Status.Unknown && valid)
            Tesla.logger.info("Raw charge state: " + source.toString());

        // The following fields aren't well defined in terms of what type and values 
        // they return. We're leaving them as String for now
        chargeStartingRange =  source.optString("charge_starting_range"); 
        chargeStartingSOC =  source.optString("charge_starting_soc"); 
        userChargeEnableRequest =  source.optString("user_charge_enable_request"); 

        chargeLimitSOC =  source.optInt("charge_limit_soc"); 
        chargeLimitSOCMax =  source.optInt("charge_limit_soc_max"); 
        chargeLimitSOCMin =  source.optInt("charge_limit_soc_min"); 
        chargeLimitSOCStd =  source.optInt("charge_limit_soc_std"); 

        euVehicle = source.optBoolean("eu_vehicle");
        chargerPhases = source.optInt("charger_phases");

        batteryHeaterOn = source.optBoolean("battery_heater_on");
        notEnoughPowerToHeat = source.optBoolean("not_enough_power_to_heat");
        fastChargerType = source.optString("fast_charger_type");
        usableBatteryLevel =  source.optInt("usable_battery_level"); 
        energyAdded = source.optDouble("charge_energy_added");
        ratedMilesAdded = source.optDouble("charge_miles_added_rated");
        idealMilesAdded = source.optDouble("charge_miles_added_ideal");
        chargeEnableRequest = source.optBoolean("charge_enable_request");
    }

    public boolean connectedToCharger() {
        return (chargingState != Status.Disconnected && chargingState != Status.Unknown);
    }
    
    public boolean isCharging() {
        return (chargingState == Status.Charging || chargeRate > 0);
    }
    
    @Override public String toString() {
        return String.format(
            "    Estimated, Ideal, Rated: (%3.1f, %3.1f, %3.1f)\n" +
            "    SOC: %d%%", 
            estimatedRange, idealRange, range, batteryPercent
            );
    }
}
