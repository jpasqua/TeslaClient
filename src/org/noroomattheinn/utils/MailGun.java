/*
 * MailGun - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Dec 11, 2013
 */

package org.noroomattheinn.utils;

import java.io.IOException;
import org.noroomattheinn.tesla.Tesla;
import us.monoid.web.FormContent;
import us.monoid.web.TextResource;

/**
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */
public class MailGun {
    private static final String SendEnpoint =
            "https://api.mailgun.net/v2/visibletesla.com/messages";

    private RestyWrapper api;

    public MailGun(String user, String auth) {
        api = new RestyWrapper();
        setAuthHeader(api, user, auth);
    }

    public boolean send(String to, String subject, String message) {
        to = to.replaceAll("\\s+", "");  // In case there is a comma-separated list of addresses
        FormContent fc = RestyWrapper.form(
                "from=notifier@visibletesla.com" +
                "&to="+to +
                "&subject="+subject + 
                "&text="+message);
        try {
            TextResource text= api.text(SendEnpoint, fc);
            if (!( text.status(200) || text.status(302) ))
                return false;
        } catch (IOException ex) {
            Tesla.logger.severe(ex.toString());
            return false;
        }
        return true;
    }

    private void setAuthHeader(RestyWrapper api, String username, String authToken) {
        byte[] authString = (username + ":" + authToken).getBytes();
        String encodedString = Utils.toB64(authString);
        api.withHeader("Authorization", "Basic " + encodedString);
    }
}
