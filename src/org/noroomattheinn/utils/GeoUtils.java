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
    
    /**
     * Calculate distance between two points in latitude and longitude taking
     * into account height difference. If you are not interested in height
     * difference pass 0.0. Uses Haversine method as its base.
     *
     * lat1, lon1 Start point lat2, lon2 End point 
     */
    public static double distance(
            double lat1, double lon1,
            double lat2, double lon2) {

        final int R = 6371; // Radius of the earth

        Double latDistance = deg2rad(lat2 - lat1);
        Double lonDistance = deg2rad(lon2 - lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        return distance;
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
}
