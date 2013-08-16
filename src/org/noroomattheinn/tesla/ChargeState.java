/*
 * ChargeState.java - Copyright(c) 2013  All Rights Reserved, Joe Pasqua
 * Created: Jul 5, 2013
 */

package org.noroomattheinn.tesla;

import org.noroomattheinn.utils.Utils;

/**
 * ChargeState: Retrieve the charging state of the vehicle.
 * NOTE: A call to refresh MUST be made before accessing the content of the
 * state. Future calls to refresh may be made to get updated versions of the
 * data.
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public class ChargeState extends APICall {
    
    
    
    //
    // Constructors
    //
    
    public ChargeState(Vehicle v) {
        super(v, Tesla.command(v.getVID(), "charge_state"));
    }

    
    //
    // Field Accessor Methods
    //
    
    public String getStateName() { return "Charge State"; }

    public boolean  chargeToMaxRange()  { return getBoolean("charge_to_max_range"); }
    public int      maxRangeCharges()   { return getInteger("max_range_charge_counter"); }
    public double   range()             { return getDouble("battery_range"); }
    public double   estimatedRange()    { return getDouble("est_battery_range"); }
    public double   idealRange()        { return getDouble("ideal_battery_range"); }
    public int      batteryPercent()    { return getInteger("battery_level"); }
    public double   batteryCurrent()    { return getDouble("battery_current"); }
    public int      chargerVoltage()    { return getInteger("charger_voltage"); }
    public double   timeToFullCharge()  { return getDouble("time_to_full_charge"); }
    public double   chargeRate()        { return getDouble("charge_rate"); }
    public boolean  chargePortOpen()    { return getBoolean("charge_port_door_open"); }
    public boolean  scheduledChargePending(){ return getBoolean("scheduled_charging_pending"); }
    public long     scheduledStart()    { return getLong("scheduled_charging_start_time"); }
    public int      chargerPilotCurrent()   { return getInteger("charger_pilot_current"); }
    public int      chargerActualCurrent()  { return getInteger("charger_actual_current"); }
    public boolean  fastChargerPresent()    { return getBoolean("fast_charger_present"); }
    public int      chargerPower()      { return getInteger("charger_power"); }
    public State    chargingState()     {
        return Utils.stringToEnum(State.class, getString("charging_state")); }

    // The following calls aren't well defined in terms of what type and values 
    // they return. We're leaving them as String for now
    public String   chargeStartingRange()      { return getString("charge_starting_range"); }
    public String   chargeStartingSOC()        { return getString("charge_starting_soc"); }
    public String   scheduledChargeStartTime() { return getString("scheduled_charging_start_time"); }
    public String   UserChargeEnableRequest()  { return getString("user_charge_enable_request"); }

    public int      chargeLimitSOC()       { return getInteger("charge_limit_soc"); }
    public int      chargeLimitSOCMax()       { return getInteger("charge_limit_soc_max"); }
    public int      chargeLimitSOCMin()       { return getInteger("charge_limit_soc_min"); }
    public int      chargeLimitSOCStd()       { return getInteger("charge_limit_soc_std"); }
    
    //
    // Nested Classes
    //
    
    public enum State {Complete, Charging, Disconnected, Unknown};

}
