/*
 * RestAPI.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Sep 28, 2013
 */

package org.noroomattheinn.utils;

import java.io.IOException;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import us.monoid.web.AbstractContent;
import us.monoid.web.FormContent;
import us.monoid.web.JSONResource;
import us.monoid.web.Resty;
import us.monoid.web.TextResource;

/**
 * RestAPI: Wraps the Resty interface and throttles the request rate
 * 
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */
public class RestAPI {

/*------------------------------------------------------------------------------
 *
 * Constants and Enums
 * 
 *----------------------------------------------------------------------------*/

    private static final Logger logger = Logger.getLogger(RestAPI.class.getName());
    
/*------------------------------------------------------------------------------
 *
 * Internal State
 * 
 *----------------------------------------------------------------------------*/

    private static Resty.Proxy proxy = null;

    private Resty resty;
    
/*==============================================================================
 * -------                                                               -------
 * -------              Public Interface To This Class                   ------- 
 * -------                                                               -------
 *============================================================================*/
        
    public RestAPI(Resty.Option... options) {
        if (proxy != null && options != null) {
            int length = options.length;
            options = Arrays.copyOf(options, length+1);
            options[length] = proxy;
        }
        resty = new Resty(options);
    }
    
    public static void setDefaultProxy(String host, int port) {
        proxy = new Resty.Proxy(host, port);
    }
    

/*------------------------------------------------------------------------------
 *
 * REST API Invocations
 * 
 *----------------------------------------------------------------------------*/
    
    public TextResource text(String anUri) throws IOException {
        return resty.text(anUri);
    }

    public TextResource text(String anUri, AbstractContent content) throws IOException {
        return resty.text(anUri, content);
    }
    
    public JSONResource json(String anUri) throws IOException {
        return resty.json(anUri);
    }
    
    public JSONResource json(String anUri, AbstractContent content) throws IOException {
        return resty.json(anUri, content);
    }
    
/*------------------------------------------------------------------------------
 *
 *  Utility Functions
 * 
 *----------------------------------------------------------------------------*/

    public static FormContent form(String query) {
        return Resty.form(query);
    }
    
	public static String enc(String unencodedString) {
        return Resty.enc(unencodedString);
    }
    
    public void withHeader(String aHeader, String aValue) {
        resty.withHeader(aHeader, aValue);
    }

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
