/*
 * RestHelper.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Sep 28, 2013
 */

package org.noroomattheinn.utils;

import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.web.Resty;

/**
 * RestHelper: Wraps the Resty interface and throttles the request rate
 * 
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */
public class RestHelper {

/*------------------------------------------------------------------------------
 *
 * Constants and Enums
 * 
 *----------------------------------------------------------------------------*/

    private static final Logger logger = Logger.getLogger(RestHelper.class.getName());
    
/*------------------------------------------------------------------------------
 *
 * Internal State
 * 
 *----------------------------------------------------------------------------*/

    private static Resty.Proxy proxy = null;
    
/*==============================================================================
 * -------                                                               -------
 * -------              Public Interface To This Class                   ------- 
 * -------                                                               -------
 *============================================================================*/
        
    public static Resty getInstance(Resty.Option... options) {
        if (proxy != null) {
            if (options != null) {
                int length = options.length;
                options = Arrays.copyOf(options, length+1);
                options[length] = proxy;
            } else {
                options = new Resty.Option[] {proxy};
            }
        }
        return new Resty(options);
    }
    
    public static void setDefaultProxy(String host, int port) {
        proxy = new Resty.Proxy(host, port);
    }
    

/*------------------------------------------------------------------------------
 *
 *  Utility Functions
 * 
 *----------------------------------------------------------------------------*/

    public static <T> void put(JSONObject jo, String key, T val) {
        try {
            jo.put(key, val);
        } catch (JSONException e) {
            logger.warning("Assert Can't Happen in put: " + e);
        }
    }

    public static JSONObject newJSONObject(String json) {
        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            logger.warning("Assert Can't Happen in newJSONObject: " + e);
            return new JSONObject();
        }
    }
    
/*------------------------------------------------------------------------------
 *
 * PRIVATE - Resty.Options for timeouts, user-agents, and throttling 
 * 
 *----------------------------------------------------------------------------*/
    
    public static class ReadTimeout extends Resty.Option {
        private int timeout;
        
        public ReadTimeout(int timeout) { this.timeout = timeout; }
        
        @Override public void apply(URLConnection aConnection) {
            aConnection.setReadTimeout(timeout);
        }
    }
        
    public static class UAOption extends Resty.Option {
        private String ua;
        
        public UAOption(String ua) { this.ua = ua; }
        
        @Override public void apply(URLConnection aConnection) {
            aConnection.setRequestProperty("User-Agent", ua);
        }
    }
        
    public static class Throttle extends Resty.Option {
        private CircularBuffer<Pair<Long,String>> timestamps = new CircularBuffer<>(200);
        private List<Pair<Integer,Integer>> rateLimits;
        
        public Throttle(List<Pair<Integer,Integer>> rateLimits) {
            this.rateLimits = rateLimits;
        }
        
        @Override public void apply(URLConnection aConnection) {
            String endpoint = aConnection.getURL().toExternalForm();
            timestamps.insert(new Pair<>(System.currentTimeMillis(), endpoint));
            while (rateLimit(endpoint)) {
                Utils.sleep(5 * 1000);
            }
        }
        
        private boolean rateLimit(String endpoint) {
            long now = System.currentTimeMillis();
            int size = timestamps.size();

            for (Pair<Integer,Integer> limit : rateLimits) {
                int count = limit.item1;
                int seconds = limit.item2;
                if (size < count) return false;

                Pair<Long,String> p = timestamps.peekAt(size - count);
                long nthRequest = p.item1;

                if ((now - nthRequest) < seconds * 1000) {
                    logger.log(
                        Level.INFO, "Throttling: More than {0} requests in {1} seconds - {2}", 
                        new Object[]{count, seconds, endpoint});
                    return true;
                }    
            }

            return false;
        }
    }
    
}
