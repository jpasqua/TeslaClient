/*
 * StreamingHandler.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 6, 2013
 */

package org.noroomattheinn.tesla.test;

import org.noroomattheinn.utils.Handler;
import org.noroomattheinn.tesla.StreamState;
import org.noroomattheinn.tesla.Streamer;
import org.noroomattheinn.tesla.Vehicle;
import org.noroomattheinn.utils.Utils;

/**
 * StreamHandler
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public class StreamingHandler extends TeslaHandler  {
    // Private Constants
    private static final String Description = "View streaming data";
    private static final String Name = "streaming";
    
    // Private Instance Variables
    //private StreamingState state;
    private StreamState state;

    
    StreamingHandler(Vehicle v) {
        super(Name, Description, "s", v);
        repl.addHandler(new StreamingHandler.DisplayHandler());
        repl.addHandler(new StreamingHandler.StreamHandler());
    }
    
    
    //
    // Handler classes
    //

    class StreamHandler extends Handler {
        StreamHandler() { super("stream", "Display streaming state", "s"); }
        @Override public boolean execute() {
            Streamer streamer = vehicle.getStreamer();
            state = streamer.refresh();
            System.out.println(state);
            for (int i = 0; i < 10; i++) {
                System.out.println("Streaming Status:");
                if ( (state = streamer.refreshFromStream()) != null) {
                    System.out.println(state);
                } else {
                    System.out.println("    [No change in state]");
                }
                Utils.sleep(500);
            }
            return true;
        }
    }
    
    class DisplayHandler extends Handler {
        DisplayHandler() { super("display", "Display snapshot state", "d"); }
        @Override public boolean execute() {
            state = vehicle.getStreamer().refresh();
            if (state != null) {
                System.out.println(state);
            }
            return true;
        }
    }

}
