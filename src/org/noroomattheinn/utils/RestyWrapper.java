/*
 * RestyWrapper.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Sep 28, 2013
 */

package org.noroomattheinn.utils;

import java.io.IOException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    private static CircularBuffer<Pair<Long,String>> timestamps = new CircularBuffer<>(1000);

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
            rateLimits.add(new Pair<>(600, 60*60)); // No more than 600 requests/hour
        } else {
            rateLimits = limits;
        }
        if (readTimeout <= 0) {
            resty = new Resty();
        } else {
            resty = new Resty(new ReadTimeoutOption(readTimeout));
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

/*------------------------------------------------------------------------------
 *
 * Static Utility Functions
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

    
/*------------------------------------------------------------------------------
 *
 * PRIVATE - Utility Classes and Methods
 * 
 *----------------------------------------------------------------------------*/
    
    private class ReadTimeoutOption extends Resty.Option {
        private int timeout;
        
        ReadTimeoutOption(int timeout) { this.timeout = timeout; }
        
        public void apply(URLConnection aConnection) {
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
