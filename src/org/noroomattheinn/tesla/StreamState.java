/*
 * StreamState.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 11, 2013
 */

package org.noroomattheinn.tesla;

import java.util.Date;
import us.monoid.json.JSONObject;

/**
 * StreamingState: Describes a result value returned from the streaming API.
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public class StreamState extends BaseState {
/*------------------------------------------------------------------------------
 *
 * Public State
 * 
 *----------------------------------------------------------------------------*/
    public long   vehicleTimestamp;
    public double speed;
    public double odometer;
    public int    soc;
    public int    elevation;
    public int    estHeading;
    public int    heading;
    public double estLat;
    public double estLng;
    public int    power;
    public String shiftState;
    public int    range;
    public int    estRange;
    
    
/*==============================================================================
 * -------                                                               -------
 * -------              Public Interface To This Class                   ------- 
 * -------                                                               -------
 *============================================================================*/
    
    public StreamState(JSONObject source) {
        super(source);
        vehicleTimestamp = source.optLong(Streamer.Keys.timestamp);
        speed = source.optDouble(Streamer.Keys.speed);
        if (Double.isNaN(speed)) speed = 0.0;
        odometer = source.optDouble(Streamer.Keys.odometer);
        soc = source.optInt(Streamer.Keys.soc);
        elevation = source.optInt(Streamer.Keys.elevation);
        estHeading = source.optInt(Streamer.Keys.est_heading);
        heading = source.optInt(Streamer.Keys.heading);
        estLat = source.optDouble(Streamer.Keys.est_lat);
        estLng = source.optDouble(Streamer.Keys.est_lng);
        power = source.optInt(Streamer.Keys.power);
        shiftState = source.optString(Streamer.Keys.shift_state);
        range = source.optInt(Streamer.Keys.range);
        estRange = source.optInt(Streamer.Keys.est_range);
    }
    
    @Override public String toString() {
        return String.format(
                "Time Stamp: %s (%s)\n" +
                "Speed: %3.1f\n" +
                "Location: [(Lat: %f, Lng: %3f), Heading: %d, Elevation: %d]\n" +
                "Charge Info: [SoC: %d, Power: %d]\n" +
                "Odometer: %7.1f\n" +
                "Range: %d\n",
                vehicleTimestamp, new Date(vehicleTimestamp),
                speed, estLat, estLng, estHeading, elevation,
                soc, power, odometer, range
                );
    }
    
}