/*
 * BaseState.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Nove 26, 2013
 */
package org.noroomattheinn.tesla;

/**
 * BaseState
 * 
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */
public class BaseState {
    
    public long timestamp;
    
    public BaseState() {
        this.timestamp = System.currentTimeMillis();
    }
}
