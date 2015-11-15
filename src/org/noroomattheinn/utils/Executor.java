/*
 * Executor.java - Copyright(c) 2014 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Oct 11, 2014
 */
package org.noroomattheinn.utils;

import java.util.Map;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;

import static org.noroomattheinn.tesla.Tesla.logger;

/**
 * Executor: Produce state updates on demand.
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */
public abstract class Executor<R extends Executor.Request> implements Runnable {
    
/*------------------------------------------------------------------------------
 *
 * Internal State
 * 
 *----------------------------------------------------------------------------*/
    
    protected final ArrayBlockingQueue<R>   queue;
    protected final String                  name;
    protected final TreeMap<Integer,Integer> histogram;
    protected final FeedbackListener        listener;
    protected       int                     nRequestsExecuted;
    
/*==============================================================================
 * -------                                                               -------
 * -------              Public Interface To This Class                   ------- 
 * -------                                                               -------
 *============================================================================*/
    
    public interface FeedbackListener {
        /**
         * Indicates that execution of the specified request has started.
         * Actually it's about to begin.
         * @param r The Request that has been started
         */
        void requestStarted(Request r);
        
        /**
         * Indicates that execution of the specified request has just completed.
         * @param r The Request that has been completed
         */
        void requestCompleted(Request r);
        
        /**
         * Provides a histogram of request failure rates on a periodic basis.
         * Each entry in the histogram is a tuple <retryCount, Count> which
         * says that Count requests were retried retryCount times before the
         * request completed. If retryCount is positive then the request
         * ultimately succeeded. If retryCount is negative then the request
         * ultimately failed.
         * @param name      The request name as returned by getRequestName()
         * @param histogram The failure rate histogram as described above.
         */
        void completionHistogram(String name, Map<Integer,Integer> histogram);
    }

    /**
     * Create an Executor which allows queued requests to be executed on a
     * separate thread.
     * @param name      The name of this Executor
     * @param listener  The FeedbackListener to which progress information
     *                  will be provided
     */
    public Executor(String name, FeedbackListener listener) {
        this.queue = new ArrayBlockingQueue<>(20);
        this.name = name;
        this.histogram = new TreeMap<>();
        this.nRequestsExecuted = 0;
        this.listener = listener;
        ThreadManager.get().launch((Runnable)this, name);
    }
    
    /**
     * Enqueue a request for later execution
     * @param r The request to be enqueued for later execution
     */
    public synchronized void produce(R r) {
        try {
            R filtered = filter(r);
            if (filtered != null) queue.put(filtered);
        } catch (InterruptedException ex) {
            logger.warning(name + " interrupted adding  to queue: " + ex.getMessage());
        }
    }
        
/*------------------------------------------------------------------------------
 *
 * Methods that must or may be implemented by subclasses
 * 
 *----------------------------------------------------------------------------*/
    
    /**
     * Determine whether the specified request, which is about to be executed,
     * has been superseded for some reason. Subclasses can override this method
     * to ignore requests that are no longer relevant. For example, a request
     * maybe irrelevant because between the time it was enqueued and the time it
     * was about to be executed, some other request already satisfied the need.
     * The default implementation always returns false.
     * @param   r   The request in question
     * @return      true if the request has been superseded
     *              false otherwise
     */
    protected boolean requestSuperseded(R r) { return false; }
    
    /**
     * Filter (replace) a request before it is enqueued. Subclasses can override
     * this method to get rid of or alter a request before it is enqueued. For
     * example, a subclass may complete remove a request if it is an unnecessary
     * duplicate of something that's already in the queue or it may alter the
     * parameters of the request if needed.
     * @param   r   The request to be filtered
     * @return      A new Request object or null. If null, no request will be
     *              enqueued. If not null, it may be the original request,
     *              a modified version of the request, or a completely different
     *              request.
     */
    protected R filter(R r) { return r; }
    
    /**
     * Execute the request. Subclasses must implement this method to implement
     * the request (e.g. perform a query, display information, whatever).
     * @param r     The request to be executed
     * @return      true if the request succeeded
     *              false otherwise
     * @throws Exception    If an Exception occurred
     */
    protected abstract boolean execRequest(R r) throws Exception;

    /**
     * Request: Base class for all request objects used by Executor
     */
    public static abstract class Request {
        public final long   timeOfRequest;
        public final Object progressContext;
        private int         nRetries;

        /**
         * Instantiate a Request object
         * @param progressContext   This contest will be passed back to the
         *                          FeedbackListener during status updates
         */
        public Request(Object progressContext) {
            this.timeOfRequest = System.currentTimeMillis();
            this.progressContext = progressContext;
            this.nRetries = 0;
        }
        
        /**
         * Returns the number of times this request has been retried. This will
         * be 0 for requests that succeeded on the first try.
         * @return  The number of times this request has been retried
         */
        int retriesPerformed() { return nRetries; }
        
        /**
         * Indicates whether the request should the request be retried.
         * The default implementation will retry maxRetries() times.
         * Subclasses can change the maxRetries() and they may also do
         * something more dynamic by overriding this method.
         * @return  true if the request should be retried
         *          false otherwise
         */
        protected boolean moreRetries() { return nRetries++ < maxRetries(); }
        
        /**
         * How many times should this request be retried.
         * Subclasses may override this if they want something other than
         * the default of 2 retries
         * @return  The maximum number of retries
         */
        protected int maxRetries() { return 2; }
        
        /**
         * How long should the executor delay before retrying a request.
         * Subclasses may override this if they want something other than
         * the default of 5 seconds (5000 ms)
         * @return  The delay time in ms
         */
        protected long retryDelay() { return 5 * 1000; }
        
        /**
         * Return the name of this request. Subclasses must override this method
         * if they want something other than "Unknown"
         * @return 
         */
        protected String getRequestName() { return "Unknown"; }
    }
    
    /**
     * Add success/failure data to the request completion histogram.
     * Subclasses may override this method if they want to be smarter or
     * more specific about what to count.
     * @param r     The request from which we are gleaning new completion info
     */
    protected void addToHistogram(Request r) {
        nRequestsExecuted++;
        int tries = r.retriesPerformed();
        if (tries > r.maxRetries()) tries = -tries;
        Integer count = histogram.get(tries);
        if (count == null) count = new Integer(0);
        histogram.put(tries, count+1);
        if (nRequestsExecuted % 10 == 0) {
            listener.completionHistogram(r.getRequestName(), histogram);
        }
    }
    
/*------------------------------------------------------------------------------
 *
 * The itnernal implementation
 * 
 *----------------------------------------------------------------------------*/
    
    private void retry(final R r) {
        ThreadManager.get().addTimedTask(new TimerTask() {
            @Override public void run() { produce(r); } },
            r.retryDelay());
    }
    
    @Override public void run() {
        while (!ThreadManager.get().shuttingDown()) {
            R r = null;
            try {
                r = queue.take();
                if (requestSuperseded(r)) continue;
                listener.requestStarted(r);
                boolean success = execRequest(r);
                listener.requestCompleted(r);
                if (!success) {
                    if (ThreadManager.get().shuttingDown()) return;
                    if (r.moreRetries()) {
                        logger.finest(r.getRequestName() + ": failed, retrying...");
                        retry(r);
                    }
                    else {
                        addToHistogram(r);
                        logger.finest(
                                r.getRequestName() + ": failed, giving up after " +
                                r.maxRetries() + " attempt(s)");
                    }
                } else {
                    addToHistogram(r);
                    logger.finest(
                            r.getRequestName() + ": Succeeded after " +
                            r.retriesPerformed()+ " attempt(s)");
                }
            } catch (Exception e) {
                if (r != null) { listener.requestCompleted(r); }
                logger.warning("Exception in " + name + ": " + e.getMessage());
                if (e instanceof InterruptedException) { return; }
            }
        }
    }

}