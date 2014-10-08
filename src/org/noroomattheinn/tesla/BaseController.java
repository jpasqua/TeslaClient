/*
 * BaseController.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 5, 2013
 */

package org.noroomattheinn.tesla;

import us.monoid.json.JSONObject;

/**
 * BaseController: A light superclass of all controllers that stores some shared
 * state and provides some convenience functions.
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public abstract class BaseController {
/*------------------------------------------------------------------------------
 *
 * Internal State
 * 
 *----------------------------------------------------------------------------*/
    protected final Tesla   tesla;
    protected final String  controllerName;
    protected JSONObject    response;
    
/*==============================================================================
 * -------                                                               -------
 * -------              Public Interface To This Class                   ------- 
 * -------                                                               -------
 *============================================================================*/
    
    public BaseController(Vehicle v, String controllerName) {
        this.tesla = v.tesla();
        this.controllerName = controllerName;
        this.response = null;
    }
    
    public Result invokeCommand(String command) { return invokeCommand(command, "{}"); }
        
    public Result invokeCommand(String command, String payload) {
        response = tesla.invokeCommand(command, payload);
        return new Result(response);
    }
        
    public JSONObject getRawResult() { return this.response; }
    
    @Override public String toString() {  return response.toString(); }
    
}
