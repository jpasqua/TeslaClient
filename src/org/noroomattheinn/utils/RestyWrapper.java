/*
 * RestyWrapper.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Sep 28, 2013
 */

package org.noroomattheinn.utils;

import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
 * RestyWrapper: Wraps the Resty interface and throttles the request rate
 * 
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */
public class RestyWrapper {

/*------------------------------------------------------------------------------
 *
 * Constants and Enums
 * 
 *----------------------------------------------------------------------------*/

    private static final Logger logger = Logger.getLogger(RestyWrapper.class.getName());
    
/*------------------------------------------------------------------------------
 *
 * Internal State
 * 
 *----------------------------------------------------------------------------*/

    private static CircularBuffer<Pair<Long,String>> timestamps = new CircularBuffer<>(100);
    private static Resty.Proxy proxy = null;
    
    private Resty resty;
    private List<Pair<Integer,Integer>> rateLimits;
    
/*==============================================================================
 * -------                                                               -------
 * -------              Public Interface To This Class                   ------- 
 * -------                                                               -------
 *============================================================================*/

/*------------------------------------------------------------------------------
 *
 * Constructors
 * 
 *----------------------------------------------------------------------------*/
    
    public RestyWrapper(int readTimeout, List<Pair<Integer,Integer>> limits) {
        if (limits == null) {
            rateLimits = new ArrayList<>();
            rateLimits.add(new Pair<>(10, 10));     // No more than 10 requests in 10 seconds
            rateLimits.add(new Pair<>(20, 60));     // No more than 20 requests/minute
            rateLimits.add(new Pair<>(150, 10*60)); // No more than 150 requests/(10 minutes)
        } else {
            rateLimits = limits;
        }
        
        if (readTimeout <= 0) {
            resty = proxy != null ? new Resty(proxy) : new Resty();
        } else {
            resty = proxy != null ? 
                    new Resty(new ReadTimeoutOption(readTimeout), proxy) :
                    new Resty(new ReadTimeoutOption(readTimeout));
        }
    }
    
    public RestyWrapper() {
        this(-1, null);
    }
    
    public RestyWrapper(List<Pair<Integer,Integer>> limits) {
        this(-1, limits);
    }
    
    public RestyWrapper(int readTimeout) {
        this(readTimeout, null);
    }
    
/*------------------------------------------------------------------------------
 *
 * REST API Invocations
 * 
 *----------------------------------------------------------------------------*/
    
    public TextResource text(String anUri) throws IOException {
        startRequest(anUri);
        return resty.text(anUri);
    }

    public TextResource text(String anUri, AbstractContent content) throws IOException {
        startRequest(anUri);
        return resty.text(anUri, content);
    }
    
    public JSONResource json(String anUri) throws IOException {
        startRequest(anUri);
        return resty.json(anUri);
    }
    
    public JSONResource json(String anUri, AbstractContent content) throws IOException {
        startRequest(anUri);
        return resty.json(anUri, content);
    }
    
/*------------------------------------------------------------------------------
 *
 *  Utility Functions
 * 
 *----------------------------------------------------------------------------*/

    public static void setProxy(String proxyHost, int proxyPort) {
        if (proxyHost == null || proxyPort < 0) proxy = null;
        else  proxy = new Resty.Proxy(proxyHost, proxyPort);
    }
    
    public static FormContent form(String query) {
        return Resty.form(query);
    }
    
	public static String enc(String unencodedString) {
        return Resty.enc(unencodedString);
    }
    
    public void withHeader(String aHeader, String aValue) {
        resty.withHeader(aHeader, aValue);
    }

    public static Map<Integer,Integer> stats() {
        int[] counts = new int[3];
        
        Arrays.fill(counts, 0);
        long now = System.currentTimeMillis();
        for (int i = timestamps.size()-1; i >= 0; i--) {
            Pair<Long,String> entry = timestamps.peekAt(i);
            long age = now - entry.item1;
            if (age < 10 * 1000) counts[0]++;
            if (age < 60 * 1000) counts[1]++;
            if (age < 60 * 60 * 1000) counts[2]++;
        }

        Map<Integer,Integer> stats = new TreeMap<>();
        stats.put(10, counts[0]);
        stats.put(60, counts[1]);
        stats.put(60*60, counts[2]);

        return stats;
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
 * PRIVATE - Utility Classes and Methods
 * 
 *----------------------------------------------------------------------------*/
    
    private class ReadTimeoutOption extends Resty.Option {
        private int timeout;
        
        ReadTimeoutOption(int timeout) { this.timeout = timeout; }
        
        @Override public void apply(URLConnection aConnection) {
            aConnection.setReadTimeout(timeout);
        }
    }
        
    private void startRequest(String endpoint) {
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
