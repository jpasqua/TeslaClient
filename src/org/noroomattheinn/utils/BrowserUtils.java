/*
 * BrowserUtils.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 11, 2013
 */
package org.noroomattheinn.utils;

/**
 * BrowserUtils
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */
import java.io.*;
import java.net.URI;

public class BrowserUtils {

    public static void popupWithURL(String url) {
        try {
            java.awt.Desktop.getDesktop().browse(URI.create(url));
        } catch (IOException e) {
            Utils.logger.info("Problem opening browser to view " + url + ": " + e);
        }
    }
    
    public static void main(String args[]) throws IOException {
        String downloadURL = "http://google.com";
        popupWithURL(downloadURL);
    }
}
