/*
 * GeoTest.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 8, 2013
 */

package org.noroomattheinn.tesla.test;

import org.noroomattheinn.utils.GeoUtils;

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
    }
}
