/*
 * GeoTest.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 8, 2013
 */

package org.noroomattheinn.tesla.test;

import java.util.ArrayList;
import java.util.List;
import org.noroomattheinn.utils.GeoUtils;
import org.noroomattheinn.utils.GeoUtils.ElevationData;

/**
 * GeoTest
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public class GeoTest {

    public static void main(String... args) {
        String address = GeoUtils.getAddrForLatLong("37.442664", "-122.184185");
                                                 //"34.115215", "-118.961334");
                                                 //"39.494503", "-98.388576");
        if (address == null)
            address = "Location cannot be determined";
        System.out.println(address);
        
        List<Loc> locations = new ArrayList<>();
        locations.add(new Loc(37.442664, -122.184185));
        locations.add(new Loc(34.115215, -122.184185));
        locations.add(new Loc(39.494503, -98.388576));
        List<ElevationData> elevations = GeoUtils.getElevations(locations);
        for (ElevationData ed : elevations) {
            System.out.println(ed.toString());
        }
    }
    
    private static class Loc implements GeoUtils.LocationSource {
        double lat;
        double lng;
        Loc(double lat, double lng) {
            this.lat = lat;
            this.lng = lng;
        }

        @Override public double getLat() { return lat; }

        @Override public double getLng() { return lng; }
    }
}
