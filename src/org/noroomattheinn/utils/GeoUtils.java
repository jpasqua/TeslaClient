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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.noroomattheinn.tesla.Tesla;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.web.Resty;

/**
 * GeoUtils
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public class GeoUtils {

    private static final Map<String,String> cache = new LRUMap<>(10);
    
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
        String resultAddr;
        Geocoder geocoder = new Geocoder();
        GeocoderRequest geocoderRequest;
        GeocodeResponse geocoderResponse;
        
        // Round to 5 digits to avoid jitter
        lat = String.format("%.5f", Double.valueOf(lat));
        lng = String.format("%.5f", Double.valueOf(lng));
        
        String cacheKey = lat+lng;
        synchronized (cache) { resultAddr = cache.get(cacheKey); }
        if (resultAddr != null)
            return resultAddr;

        geocoderRequest = new GeocoderRequestBuilder()
                .setLocation(new LatLng(lat, lng))
                .setLanguage("en").getGeocoderRequest();
        geocoderResponse = geocoder.geocode(geocoderRequest);
        if (geocoderResponse != null) {
            if (geocoderResponse.getStatus() == GeocoderStatus.OK) {
                if (!geocoderResponse.getResults().isEmpty()) {
                    GeocoderResult geocoderResult = // Get the first result
                            geocoderResponse.getResults().iterator().next();
                    resultAddr = geocoderResult.getFormattedAddress();
                    synchronized (cache) { cache.put(cacheKey, resultAddr); }
                    return resultAddr;
                }
            }
        }
        return null;
    }
    
    public static double[] getLatLngForAddr(String addr) {
        if (addr == null) return null;
        
        Geocoder geocoder = new Geocoder();
        GeocoderRequest geocoderRequest;
        GeocodeResponse geocoderResponse;

        geocoderRequest = new GeocoderRequestBuilder()
                .setAddress(addr)
                .setLanguage("en").getGeocoderRequest();
        geocoderResponse = geocoder.geocode(geocoderRequest);
        if (geocoderResponse != null) {
            if (geocoderResponse.getStatus() == GeocoderStatus.OK) {
                if (!geocoderResponse.getResults().isEmpty()) {
                    GeocoderResult geocoderResult = // Get the first result
                            geocoderResponse.getResults().iterator().next();
                    double[] loc = new double[2];
                    LatLng ll = geocoderResult.getGeometry().getLocation();
                    loc[0] = ll.getLat().doubleValue();
                    loc[1] = ll.getLng().doubleValue();
                    return loc;
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
    
    private static final String ElevationEndpoint =
            "http://maps.googleapis.com/maps/api/elevation/";
    private static final int MaxLocationsPerRequest = 50;
    private enum GoogleElevationStatus {OK, INVALID_REQUEST, OVER_QUERY_LIMIT,
                                        REQUEST_DENIED, UNKNOWN_ERROR};
    
    public static List<ElevationData> getElevations(List<? extends LocationSource> locations) {
        List<ElevationData> elevations = new ArrayList<>();
        int nLocs = locations.size();
        int startIndex = 0;
        int thisChunk = Math.min(nLocs, MaxLocationsPerRequest);
        
        while (thisChunk > 0) {
            List<ElevationData> results = getElevInternal(
                    locations.subList(startIndex, startIndex+thisChunk));
            if (results == null) return null;
            elevations.addAll(results);
            startIndex += thisChunk;
            nLocs -= thisChunk;
            thisChunk = Math.min(nLocs, MaxLocationsPerRequest);
        }
        return elevations;
    }
    
    private static List<ElevationData> getElevInternal(List<? extends LocationSource> locations) {
        List<ElevationData> elevations = new ArrayList<>();
        Resty elevationAPI = new Resty();
        int nLocs = locations.size();
        
        StringBuilder locs = new StringBuilder("json?locations=");
        for (int i = 0; i < nLocs; i++) {
            if (i != 0) locs.append("%7C"); // Pipe Symbol, '|'
            LocationSource location = locations.get(i);
            locs.append(location.getLat()).append(',').append(location.getLng());
        }

        try {
            JSONObject result = elevationAPI.json(
                    ElevationEndpoint+locs.toString()+"&sensor=true").object();
            GoogleElevationStatus status = GoogleElevationStatus.valueOf(
                    result.optString("status"));
            if (status != GoogleElevationStatus.OK) {
                Tesla.logger.warning(
                        "Error retrieving elevation data." +
                        "Status returned by Google = " + status);
                return null;
            }
            JSONArray vals = result.getJSONArray("results");
            for (int i = 0; i < vals.length(); i++) {
                JSONObject cur = vals.getJSONObject(i);
                double elevation = cur.optDouble("elevation");
                double resolution = cur.optDouble("resolution");
                LocationSource location = locations.get(i);
                elevations.add(new ElevationData(
                        location.getLat(), location.getLng(), elevation, resolution));
            }
            return elevations;
        } catch (IOException | JSONException ex) {
            Tesla.logger.warning("Error retrieving elevation data: " + ex.getMessage());
            return null;
        }
    }
    
    public static interface LocationSource {
        double getLat();
        double getLng();
    }
    
    public static class ElevationData {
        public double lat;
        public double lng;
        public double elevation;
        public double resolution;
        
        ElevationData(double lat, double lng, double elevation, double resolution) {
            this.lat = lat;
            this.lng = lng;
            this.elevation = elevation;
            this.resolution = resolution;
        }
        
        @Override public String toString() {
            return String.format(
                    "[\n" +
                    "    Location: {%f,%f},\n" +
                    "    Elevation: %f\n" +
                    "    Resoultion: %f\n" +
                    "]\n", lat, lng, elevation, resolution);
        }
    }
    
    public static abstract class Area implements LocationSource, Comparable<Area> {
        public final double lat;
        public final double lng;
        public final String name;

        public Area(double lat, double lng, String name) {
            this.lat = lat;
            this.lng = lng;
            this.name = name;
        }
        
        @Override public double getLat() { return lat; }

        @Override public double getLng() { return lng; }
        
        public String getName() { return name; }
        
        /**
         * An implementation of compareTo that really is about intersection.
         * Subclasses must override this method.
         * @param other The other Area to compare to
         * @return  -1  If the 2 areas do not intersect
         *           0  If the 2 areas intersect
         *           1  Never returned
         */
        @Override abstract public int compareTo(Area o);
    }
    
    public static class CircularArea extends Area {
        public final double radius;
        
        public CircularArea() {
            this(0, 0, 0, "-");
        }

        public CircularArea(double lat, double lng, double radius, String name) {
            super(lat, lng, name);
            this.radius = radius;
        }

        @Override public int compareTo(Area other) {
            if (intersects(other)) return 0;
            return -1;
        }
            
        public boolean intersects(Area other) {
            if (other instanceof CircularArea) {
                double distance = GeoUtils.distance(lat, lng, other.lat, other.lng);
                double coverage = radius + ((CircularArea)other).radius;
                return distance <= coverage;
            }
            return false;
        }

    }
}
