/*
 * CalTime.java - Copyright(c) 2014 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Oct 19, 2013
 */
package org.noroomattheinn.utils;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * A calendar that only cares about hours and minutes. The date is set to an
 * arbitrary but consistent value in the past. Seconds are set to zero.
 * 
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */
public class CalTime extends GregorianCalendar {

    /**
     * Create a new instance that is initialized from a String representation
     * of a CalTime. This is the same representation that is used by toString
     * when externalizing.
     * @param time A String of the form: HH^MM^AP, where AP is either AM or PM
     */
    public CalTime(String time) {
        internalize(time);
    }

    /**
     * Create a new instance that is initialized from a set of Strings
     * @param hh    The hour as a String. Must be in the range 0-12
     * @param mm    The minute as a String. Must be in the range 0-59
     * @param amPM  AM or PM
     */
    public CalTime(String hh, String mm, String amPM) {
        internalize(hh + "^" + mm + "^" + amPM);
    }

    public CalTime(long millis) {
        this.setTimeInMillis(millis);
        normalizeDate();
    }
    
    /**
     * Returns a String representation of the CalTime. 
     * @return A String of the form: HH^MM^AP, where AP is either AM or PM
     */
    @Override public final String toString() {
        return String.format("%02d^%02d^%s",
                get(Calendar.HOUR),
                get(Calendar.MINUTE),
                get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM");
    }

    private void internalize(String time) {
        normalizeDate();
        String items[] = time.split("\\^");
        set(Calendar.HOUR, Integer.valueOf(items[0]));
        set(Calendar.MINUTE, Integer.valueOf(items[1]));
        set(Calendar.AM_PM, items[2].equals("AM") ? Calendar.AM : Calendar.PM);
    }
    
    private void normalizeDate() {
        set(1960, 05, 20);
        set(Calendar.SECOND, 0);
    }
}
