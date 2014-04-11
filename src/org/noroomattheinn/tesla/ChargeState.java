/*
 * ChargeState.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 5, 2013
 */

package org.noroomattheinn.tesla;

import org.noroomattheinn.utils.Utils;

/**
 * ChargeState: Retrieve the charging state of the vehicle.
 * NOTE: A call to refresh MUST be made before accessing the content of the
 * state. Future calls to refresh may be made to get updated versions of the
 * state.
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public class ChargeState extends APICall {
    
    public State state;
    
    //
    // Constructors
    //
    
    public ChargeState(Vehicle v) {
        super(v, Tesla.command(v.getVID(), "charge_state"));
    }

    @Override protected BaseState setState(boolean valid) {
        return (state = valid ? new State(this) : null);
    }
    
    //
    // Field Accessor Methods
    //
    
    @Override public String getStateName() { return "Charge State"; }
    
    //
    // Nested Classes
    //
    
    public enum Status {Complete, Charging, Disconnected, Stopped, NoPower, Starting, Unknown};

    public static class State extends BaseState {
        public boolean  chargeToMaxRange;
        public int      maxRangeCharges;
        public double   range;
        public double   estimatedRange;
        public double   idealRange;
        public int      batteryPercent;
        public double   batteryCurrent;
        public int      chargerVoltage;
        public double   timeToFullCharge;
        public double   chargeRate;
        public boolean  chargePortOpen;
        public boolean  scheduledChargePending;
        public long     scheduledStart;
        public int      chargerPilotCurrent;
        public int      chargerActualCurrent;
        public boolean  fastChargerPresent;
        public int      chargerPower;
        public Status    chargingState;

        // The following calls aren't well defined in terms of what type and values 
        // they return. We're leaving them as String for now
        public String   chargeStartingRange;
        public String   chargeStartingSOC;
        public String   scheduledChargeStartTime;
        public String   UserChargeEnableRequest;

        public int      chargeLimitSOC;
        public int      chargeLimitSOCMax;
        public int      chargeLimitSOCMin;
        public int      chargeLimitSOCStd;
        
        public boolean  euVehicle;
        public int      chargerPhases;
        
        public State(ChargeState cs) {
            chargeToMaxRange =  cs.getBoolean("charge_to_max_range"); 
            maxRangeCharges =  cs.getInteger("max_range_charge_counter"); 
            range =  cs.getDouble("battery_range"); 
            estimatedRange =  cs.getDouble("est_battery_range"); 
            idealRange =  cs.getDouble("ideal_battery_range"); 
            batteryPercent =  cs.getInteger("battery_level"); 
            batteryCurrent =  cs.getDouble("battery_current"); 
            chargerVoltage =  cs.getInteger("charger_voltage"); 
            timeToFullCharge =  cs.getDouble("time_to_full_charge"); 
            chargeRate =  cs.getDouble("charge_rate"); 
            chargePortOpen =  cs.getBoolean("charge_port_door_open"); 
            scheduledChargePending = cs.getBoolean("scheduled_charging_pending"); 
            scheduledStart =  cs.getLong("scheduled_charging_start_time");
            try {
                chargerPilotCurrent =  cs.getRawResult().getInt("charger_pilot_current");
            } catch (Exception e) {
                chargerPilotCurrent = -1;
                Tesla.logger.finest("Pilot Current is null");
            }
            chargerActualCurrent =  cs.getInteger("charger_actual_current"); 
            fastChargerPresent =  cs.getBoolean("fast_charger_present"); 
            chargerPower =  cs.getInteger("charger_power"); 
            chargingState =  Utils.stringToEnum(Status.class, cs.getString("charging_state"));
            if (chargingState == Status.Unknown)
                Tesla.logger.info("Raw charge state: " + cs.toString());

            // The following calls aren't well defined in terms of what type and values 
            // they return. We're leaving them as String for now
            chargeStartingRange =  cs.getString("charge_starting_range"); 
            chargeStartingSOC =  cs.getString("charge_starting_soc"); 
            scheduledChargeStartTime =  cs.getString("scheduled_charging_start_time"); 
            UserChargeEnableRequest =  cs.getString("user_charge_enable_request"); 

            chargeLimitSOC =  cs.getInteger("charge_limit_soc"); 
            chargeLimitSOCMax =  cs.getInteger("charge_limit_soc_max"); 
            chargeLimitSOCMin =  cs.getInteger("charge_limit_soc_min"); 
            chargeLimitSOCStd =  cs.getInteger("charge_limit_soc_std"); 
            
            euVehicle = cs.getBoolean("eu_vehicle");
            chargerPhases = cs.getInteger("charger_phases");
        }
    }
}
