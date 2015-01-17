/*
 * MailGun - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Dec 11, 2013
 */

package org.noroomattheinn.utils;

import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.noroomattheinn.tesla.Tesla;
import static org.noroomattheinn.tesla.Tesla.logger;
import us.monoid.web.FormContent;
import us.monoid.web.TextResource;

/**
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */
public class MailGun {
    private static MailGun defaultInstance = null;
    
    private static final String SendEnpoint =
            "https://api.mailgun.net/v2/visibletesla.com/messages";

    private RestAPI api;

    public static void createDefaultInstance(String user, String auth) {
        defaultInstance = new MailGun(user, auth);
    }
    
    public static MailGun get() { return defaultInstance; }
    
    public MailGun(String user, String auth) {
        api = new RestAPI();
        setAuthHeader(api, user, auth);
    }

    public boolean send(String to, String message) {
        final int SubjectLength = 30;
        String subject = StringUtils.left(message, SubjectLength);
        if (message.length() > SubjectLength) {
            subject = subject + "...";
        }
        return send(to, subject, message);
    }

    public boolean send(String to, String subject, String message) {
        if (subject == null) subject = "";
        if ((message == null || message.isEmpty()) && subject.isEmpty()) {
            logger.warning("No message or subject specified, message not sent");
            return false;
        }
        if (to == null || to.isEmpty()) {
            logger.warning("No recipient specified, message not sent: " + message);
            return false;
        }
        to = to.replaceAll("\\s+", "");  // In case there is a comma-separated list of addresses
        FormContent fc = RestAPI.form(
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

    private void setAuthHeader(RestAPI api, String username, String authToken) {
        byte[] authString = (username + ":" + authToken).getBytes();
        String encodedString = Utils.toB64(authString);
        api.withHeader("Authorization", "Basic " + encodedString);
    }
}
