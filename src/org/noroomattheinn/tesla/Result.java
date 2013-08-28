/*
 * Result.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 5, 2013
 */

package org.noroomattheinn.tesla;

/**
 * Result: Many of the REST calls result in the following snippet of JSON:
 * <code>{reason:String, result:boolean}</code>. This class takes a JSONResult
 * which is presumed to have that structure and creates an object that represents
 * those values.
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public class Result {
    // Instance Variables
    public boolean success;
    public String explanation;
    
    
    //
    // Constructors
    //
    
    public Result(APICall result) {
        success = result.getBoolean("result");
        explanation = result.getString("reason");
    }
    
    public Result(boolean success, String explanation) {
        this.success = success;
        this.explanation = explanation;
    }
    
    public static final Result Succeeded = new Result(true, "");
    public static final Result Failed = new Result(false, "");
    
}
