/*
 * GeoUtils.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 10, 2013
 */

package org.noroomattheinn.utils;

import com.google.code.geocoder.Geocoder;
import com.google.code.geocoder.GeocoderRequestBuilder;
import com.google.code.geocoder.model.GeocodeResponse;
import com.google.code.geocoder.model.GeocoderRequest;
import com.google.code.geocoder.model.GeocoderResult;
import com.google.code.geocoder.model.GeocoderStatus;
import com.google.code.geocoder.model.LatLng;

/**
 * GeoUtils
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public class GeoUtils {

    //
    // Public Class Methods
    //
    
    public static String headingToString(double x) {
            String directions[] = {
                "North", "North East", "East", "South East",
                "South", "South West", "West", "North West", "North"};
            return directions[ (int) Math.round((((double) x % 360) / 45))];
        }

    public static String getAddrForLatLong(String lat, String lng) {
        Geocoder geocoder = new Geocoder();
        GeocoderRequest geocoderRequest;
        GeocodeResponse geocoderResponse;

        geocoderRequest = new GeocoderRequestBuilder()
                .setLocation(new LatLng(lat, lng))
                .setLanguage("en").getGeocoderRequest();
        geocoderResponse = geocoder.geocode(geocoderRequest);
        if (geocoderResponse != null) {
            if (geocoderResponse.getStatus() == GeocoderStatus.OK) {
                if (!geocoderResponse.getResults().isEmpty()) {
                    GeocoderResult geocoderResult = // Get the first result
                            geocoderResponse.getResults().iterator().next();
                    return geocoderResult.getFormattedAddress();
                }
            }
        }
        return null;
    }
}
