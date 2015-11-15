/*
 * SimpleTemplate.java -  - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Nov 28, 2013
 */

package org.noroomattheinn.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.logging.Level;
import org.noroomattheinn.tesla.Tesla;

/**
 * SimpleTemplate: Really simple & slow tools for replacing fields in a template.
 * 
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */
public class SimpleTemplate {

/*------------------------------------------------------------------------------
 *
 * Internal State
 * 
 *----------------------------------------------------------------------------*/
    
    private StringBuilder sb;
    
    
/*==============================================================================
 * -------                                                               -------
 * -------              Public Interface To This Class                   ------- 
 * -------                                                               -------
 *============================================================================*/
    
    public SimpleTemplate(StringBuilder sb) { this.sb = sb; }
    
    public SimpleTemplate(InputStream is) { this.sb = fromInputStream(is); }
    
    public String fillIn(Map<String,String> replacements) {
        for (Map.Entry<String,String> e : replacements.entrySet()) {
            String fieldName = e.getKey();
            String text = e.getValue();
            replaceField(fieldName, text);
        }
        return sb.toString();
    }
    
    public String fillIn(String... fields) {
        int size = fields.length;
        if (size % 2 == 1) throw new IllegalArgumentException("Mismatched number of key/val pairs");
        for (int i = 0; i < size; i += 2) {
            String fieldName = fields[i];
            String text = fields[i+1];
            replaceField(fieldName, text);
        }
        return sb.toString();
    }
    
/*------------------------------------------------------------------------------
 *
 * PRIVATE - Utility Methods
 * 
 *----------------------------------------------------------------------------*/
        
    private void replaceField(String placeholder, String newText) {
        int length = placeholder.length();
        int loc = sb.indexOf(placeholder);
        sb.replace(loc, loc+length, newText);
    }
    
    private StringBuilder fromInputStream(InputStream is) {
        StringBuilder builder = new StringBuilder();
        InputStreamReader r = new InputStreamReader(is);
        try {
            int c;
            while ((c = r.read()) != -1) { builder.append((char) c); }
        } catch (IOException ex) {
            Tesla.logger.log(Level.SEVERE, null, ex);
        }
        return builder;
    }
    
}
