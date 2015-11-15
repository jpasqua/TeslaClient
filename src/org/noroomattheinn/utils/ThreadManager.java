/*
 * ThreadManager - Copyright(c) 2013, 2014 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Aug 16, 2014
 */

package org.noroomattheinn.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;

/**
 * ThreadManager: Manage (start, stop, cleanup) threads used by the app
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */
public class ThreadManager {
    
/*------------------------------------------------------------------------------
 *
 * Internal State
 * 
 *----------------------------------------------------------------------------*/
    
    private static ThreadManager instance = null;
    private static final Logger logger = Logger.getLogger(ThreadManager.class.getName());
    
    private int threadID = 0;
    
    private final ArrayList<Thread> threads;
    private final List<Stoppable>   stopList;
    private final Timer             timer;
    private boolean                 shuttingDown;
    
/*==============================================================================
 * -------                                                               -------
 * -------              Public Interface To This Class                   ------- 
 * -------                                                               -------
 *============================================================================*/
    

    public static ThreadManager create() {
        if (instance != null) return instance;
        return (instance = new ThreadManager());
    }
    
    public static ThreadManager get() { return instance; }
    
    public interface Stoppable { public void stop(); }
    
    public boolean shuttingDown() { return shuttingDown; }
    
    
    public synchronized Thread launch(Runnable r, String name) {
        if (shuttingDown) return null;
        
        Thread t = new Thread(r);
        if (name == null) name = String.valueOf(threadID++);
        t.setName("00 VT - " + name);
        t.setDaemon(true);
        t.start();
        threads.add(t);

        // Clean out any old terminated threads...
        Iterator<Thread> i = threads.iterator();
        while (i.hasNext()) {
            Thread cur = i.next();
            if (cur.getState() == Thread.State.TERMINATED) {
                i.remove();
            }
        }

        return t;
    }
    
    public void addTimedTask(TimerTask task, long delay) {
        timer.schedule(task, delay);
    }
    
    public void addStoppable(Stoppable s) { stopList.add(s); }
    
    public synchronized void shutDown() {
        shuttingDown = true;
        timer.cancel();
        for (Stoppable s : stopList) { s.stop(); }
        
        int nActive;
        do {
            nActive = 0;
            logger.finest("Iterating through terminate loop");
            for (Thread t : threads) {
                Thread.State state = t.getState();
                switch (state) {
                    case NEW:
                    case RUNNABLE:
                        nActive++;
                        logger.finest("Active thread: " + t.getName());
                        break;

                    case TERMINATED:
                        logger.finest("Terminated thread: " + t.getName());
                        break;

                    case BLOCKED:
                    case TIMED_WAITING:
                    case WAITING:
                        logger.finest("About to interrupt thread: " + t.getName());
                        nActive++;
                        t.interrupt();
                        Utils.yieldFor(100);
                        break;

                    default:
                        break;
                }
            }
        } while (nActive > 0);
    }
    
    public void sleep(long timeInMillis) { Utils.sleep(timeInMillis,  sdPredicate); }

    private Utils.Predicate sdPredicate = new Utils.Predicate() {
            @Override public boolean eval() { return shuttingDown; } };
    
/*------------------------------------------------------------------------------
 *
 * Launch and manage external processes
 * 
 *----------------------------------------------------------------------------*/
    
    public Process launchExternal(String command, String args, String input, long timeout) {
        String fullCommand = command + " " + (args == null ? "" : args);
        try {
            Process p = Runtime.getRuntime().exec(fullCommand);
            if (input != null) {
                IOUtils.copy(IOUtils.toInputStream(input), p.getOutputStream());
                p.getOutputStream().close();
            }
            
            watch(command, p, timeout); // Launch a watchdog thread
            return p;
        } catch (IOException ex) {
            logger.warning("External command (" + fullCommand + ") failed to launch: " + ex);
            return null;
        }
    }
    
    private int wdID = 0;
    private void watch(final String name, final Process p, final long timeout) {
        Runnable watchdog = new Runnable() {
            @Override public void run() {
                long targetTime = System.currentTimeMillis() + timeout;
                while (System.currentTimeMillis() < targetTime) {
                    if (hasExited(p)) {
                        int exitVal = p.exitValue();
                        logger.info("External process completed: " + name + "(" + exitVal + ")");
                        return;
                    }
                    Utils.sleep(Math.min(5 * 1000, targetTime - System.currentTimeMillis()));
                }
                // p hasn't terminated yet! Kill it.
                p.destroy();
                logger.warning("External process timed out - killing it: " + name);
            }
        };
        
        launch(watchdog, String.format("Watchdog %d", wdID++));
    }
    
    private boolean hasExited(Process p) {
        try {
            p.exitValue();  // An exception will be raised if it hasn't exited yet
            return true;
        } catch (IllegalThreadStateException e) {
            return false;
        }
    }
    
/*------------------------------------------------------------------------------
 *
 * Hide the constructor
 * 
 *----------------------------------------------------------------------------*/
    
    private ThreadManager() {
        this.threads = new ArrayList<>();
        this.stopList = new ArrayList<>();
        this.timer = new Timer();
        this.shuttingDown = false;
    }
}

