/*
 * BaseState.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Nove 26, 2013
 */
package org.noroomattheinn.tesla;

import org.noroomattheinn.utils.RestAPI;
import us.monoid.json.JSONObject;

/**
 * BaseState: A lightweight superclass of all State objects. Stores some shared
 * public state and does some common initialization in the constructor.
 * 
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */
public abstract class BaseState {
/*==============================================================================
 * -------                                                               -------
 * -------              Public Interface To This Class                   ------- 
 * -------                                                               -------
 *============================================================================*/

    protected static final JSONObject emptyJSONObj = RestAPI.newJSONObject("{}");
    
    public final long         timestamp;
    public final JSONObject   rawState;
    public final boolean      valid;
    
    public BaseState(JSONObject rawState) {
        this.timestamp = System.currentTimeMillis();
        this.rawState = rawState;
        valid = (rawState.length() > 0);
    }
}
