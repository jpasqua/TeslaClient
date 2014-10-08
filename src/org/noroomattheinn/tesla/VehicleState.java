/*
 * VehicleState.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 5, 2013
 */

package org.noroomattheinn.tesla;

import org.noroomattheinn.utils.Utils;
import us.monoid.json.JSONObject;

/**
 * VehicleState: Contains an assortment of information about the current state
 * of the vehicle. This includes things like whether the doors are open, whether
 * the car is locked, what version of firmware is installed, and a lot of other
 * odds and ends.
 * 
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */
public class VehicleState extends BaseState {
/*------------------------------------------------------------------------------
 *
 * Constants and Enums
 * 
 *----------------------------------------------------------------------------*/
    public enum PanoPosition {open, closed, vent, comfort, moving, unknown, Unknown};
    
/*------------------------------------------------------------------------------
 *
 * Public State
 * 
 *----------------------------------------------------------------------------*/
    public final  boolean  isDFOpen;
    public final  boolean  isPFOpen;
    public final  boolean  isDROpen;
    public final  boolean  isPROpen;
    public final  boolean  isFTOpen;
    public final  boolean  isRTOpen;
    public final  boolean  locked;
    public final  boolean  hasPano;
    public final  int      panoPercent;
    public final  PanoPosition panoState;
    public final  String   version;
    public final  boolean  hasDarkRims;
    public final  String   wheelType;
    public final  boolean  hasSpoiler;
    public final  String   roofColor;
    public final  String   perfConfig;
    public final  boolean  remoteStart;
    
/*==============================================================================
 * -------                                                               -------
 * -------              Public Interface To This Class                   ------- 
 * -------                                                               -------
 *============================================================================*/
    
    public VehicleState(JSONObject source) {
        super(source);
        isDFOpen = source.optInt("df") != 0;
        isPFOpen = source.optInt("pf") != 0;
        isDROpen = source.optInt("dr") != 0;
        isPROpen = source.optInt("pr") != 0;
        isFTOpen = source.optInt("ft") != 0;
        isRTOpen = source.optInt("rt") != 0;
        locked = source.optBoolean("locked");
        hasPano = source.optBoolean("sun_roof_installed");
        panoPercent = source.optInt("sun_roof_percent_open");
        panoState = Utils.stringToEnum(
                VehicleState.PanoPosition.class, source.optString("sun_roof_state"));
        version = source.optString("car_version");
        hasDarkRims = source.optBoolean("dark_rims");
        wheelType = source.optString("wheel_type");
        hasSpoiler = source.optBoolean("has_spoiler");
        roofColor = source.optString("roof_color");
        perfConfig = source.optString("perf_config");
        remoteStart = source.optBoolean("remote_start");
    }
    
    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        if (isDFOpen) sb.append("    Driver Front Door is open\n");
        if (isDROpen) sb.append("    Driver Rear Door is open\n");
        if (isPFOpen) sb.append("    Passenger Front Door is open\n");
        if (isPROpen) sb.append("    Passenger Rear Door is open\n");
        if (isFTOpen) sb.append("    Frunk is open\n");
        if (isRTOpen) sb.append("    Trunk is open\n");
        sb.append("    The car is "); 
        sb.append(locked ? "locked\n" : "unlocked\n");
        if (hasPano) {
            sb.append("    Panoramic roof: ");
            if (panoState != PanoPosition.unknown)
                sb.append(panoState);
            sb.append(" ("); sb.append(panoPercent); sb.append("%)\n");
        }
        
        sb.append("    Remote start is ");
        if (!remoteStart) sb.append("not ");
        sb.append("enabled\n");
        
        sb.append("    Firmware version: "); sb.append(version);
        return sb.toString();
    }
    
}
