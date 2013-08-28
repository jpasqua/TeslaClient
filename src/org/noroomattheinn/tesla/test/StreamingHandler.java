/*
 * StreamingHandler.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 6, 2013
 */

package org.noroomattheinn.tesla.test;

import org.noroomattheinn.utils.Handler;
import org.noroomattheinn.tesla.StreamingState;
import org.noroomattheinn.tesla.Vehicle;

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
    private StreamingState state;

    
    StreamingHandler(Vehicle v) {
        super(Name, Description, v);
        state = new StreamingState(v);
        repl.addHandler(new StreamingHandler.DisplayHandler());
    }
    
    
    //
    // Handler classes
    //

    class DisplayHandler extends Handler {
        DisplayHandler() { super("display", "Display streaming state", "d"); }
        public boolean execute() {
            System.out.println("Streaming Status:");
            while (state.refresh()) {
                System.out.println(state);
            }
            return true;
        }
    }

}
