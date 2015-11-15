/*
 * Result.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 5, 2013
 */

package org.noroomattheinn.tesla;

import us.monoid.json.JSONObject;

/**
 * Result: Many of the REST calls result in the following snippet of JSON:
 * <code>{reason:String, result:boolean}</code>. This class takes a JSONResult
 * which is presumed to have that structure and creates an object that represents
 * those values.
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public class Result {
/*------------------------------------------------------------------------------
 *
 * Public State
 * 
 *----------------------------------------------------------------------------*/

    public final boolean success;
    public final String explanation;
    
/*==============================================================================
 * -------                                                               -------
 * -------              Public Interface To This Class                   ------- 
 * -------                                                               -------
 *============================================================================*/
    
    public Result(JSONObject response) {
        if (response == null) {
            success = false;
            explanation = "";
        } else {
            success = response.optBoolean("result", false);
            explanation = response.optString("reason");
        }
    }
    
    public Result(boolean success, String explanation) {
        this.success = success;
        this.explanation = explanation;
    }
    
    public static final Result Succeeded = new Result(true, "");
    public static final Result Failed = new Result(false, "");
    
}
