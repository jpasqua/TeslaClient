/*
 * StateAPI.java - Copyright(c) 2014 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Sep 14, 2014
 */

package org.noroomattheinn.tesla;

/**
 * StateAPI: This class is the parent of all API interactions for State.
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public abstract class StateAPI extends APICall {
    
    // Instance Variables
    private String endpoint;
    
    //
    // Constructors
    //
    
    public StateAPI(Vehicle v, String endpoint, String name) {
        super(v, name);
        this.endpoint = endpoint;
    }
    
    abstract protected void setState(boolean valid);
    
    //
    // Updating the endpoint and refreshing the state
    //
    
    public boolean refresh() {
        boolean success = getState(endpoint);
        setState(success);
        return success;
    }
    
}
