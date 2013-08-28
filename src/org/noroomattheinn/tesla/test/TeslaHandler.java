/*
 * TeslaHandler.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 6, 2013
 */

package org.noroomattheinn.tesla.test;

import org.noroomattheinn.utils.Handler;
import org.noroomattheinn.utils.REPL;
import org.noroomattheinn.tesla.Vehicle;

/**
 * TeslaHandler
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public class TeslaHandler extends Handler  {
    
    protected REPL repl;
    protected Vehicle vehicle;

    public boolean execute() {
        repl.repl();
        return true;
    }

    TeslaHandler(String name, String description, String alias, Vehicle v) {
        super(name, description, alias);
        this.vehicle = v;
        repl = new REPL(name);
    }

    TeslaHandler(String name, String description, Vehicle v) {
        this(name, description, null, v);
    }

}