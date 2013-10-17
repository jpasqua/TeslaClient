/*
 * Tesla.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 5, 2013
 */

package org.noroomattheinn.tesla;

import java.io.File;
import java.io.IOException;
import java.net.HttpCookie;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.noroomattheinn.utils.CookieUtils;
import org.noroomattheinn.utils.RestyWrapper;
import us.monoid.json.JSONArray;
import us.monoid.json.JSONException;
import us.monoid.web.FormContent;
import us.monoid.web.TextResource;

/**
 * Tesla: This class is the starting point for communicating with Tesla's
 * portal for querying and controlling Tesla vehicles.
 * <P>
 * The basic sequence of events is:<ol>
 * <li>Create a Tesla object
 * <li>Connect to the Tesla portal by passing login credentials to the
 * connect() method
 * <li>Get a list of Vehicles associated with the account
 * <li>Query and control a vehicle using the State and Controller objects by
 * supplying a Vehicle object
 * </ol>
 * We use a persistent cookie store to maintain login credentials and sessions.
 * We also use the cookie store to persist other local information like
 * the username. We need to keep that around even if we have auth cookies because
 * the Streaming API needs it. Rather than ask the user for it again, we keep
 * it from the first time she logs in using it.
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public class Tesla {
    // Constants
    private static final String TeslaURI = "https://portal.vn.teslamotors.com";
    private static final URI LocalURI = URI.create("local");
    private static final String UsernameKey = "username";
    private static final String CookiesFile = "cookies.txt";
    private static final String LogFileName = "tesla.log";
    
    // Class Variables
    public static final Logger logger = Logger.getLogger(Tesla.class.getName());
    static {
        FileHandler fileHandler;
        try {
            fileHandler = new FileHandler(LogFileName);
            logger.addHandler(fileHandler);
        } catch (IOException | SecurityException ex) {
            logger.log(Level.SEVERE, "Unable to establish log file");
        }
    }
    
    // Instance Variables
    private final RestyWrapper api;
    private final List<Vehicle> vehicles;
    private String username = null;
    
    
    //
    // Constructors
    //
    
    public Tesla() {
        api = new RestyWrapper();
        vehicles = new ArrayList<>();
    }

    
    //
    // Methods to build endpoint strings
    //
    
    public static String command(String vid, String command) {
        return endpoint(vid, "command/"+command);
    }

    public static String endpoint(String vid, String name) {
        return TeslaURI + "/vehicles/" + vid + "/" + name;
    }
    
    public static String endpoint(String name) {
        return TeslaURI + "/" + name;
    }
    

    
    //
    // Fetching and storing username
    //
    
    private void grabUsername() {
        HttpCookie c = CookieUtils.getCookie(UsernameKey, LocalURI);
        if (c != null)
            username = c.getValue();
    }

    private void stashUsername() {
        HttpCookie c = new HttpCookie(UsernameKey, username);
        CookieUtils.addCookie(c, LocalURI);
    }
    
    
    //
    // login / connect Methods
    // 
    
    private boolean login() {
        if (CookieUtils.readAndStoreCookies(CookiesFile)) {
            grabUsername();
            return true;
        }
        return false;
    }
    
    private boolean login(String username, String password) {
        try {
            TextResource text;
            
            CookieUtils.clearCookies();
            // Do the first phase of the login. This sets the _s_portal_session
            // cookie. This must be followed by phase 2 of the login
            text = api.text(endpoint("login"));
            if (!( text.status(200) || text.status(302) ))
                return false;
            
            // OK, now complete the login  by doing a POST with the username
            // and password. This sets the user_credentials cookie
            FormContent fc = RestyWrapper.form(
                    "user_session[email]=" + RestyWrapper.enc(username) +
                    "&user_session[password]=" + RestyWrapper.enc(password));
            text = api.text(endpoint("login"), fc);
            int status = text.http().getResponseCode();
            if (!text.status(200) && !text.status(302))
                return false;
            
            String responseBody = text.toString();
            if (responseBody.contains("You do not have access")) {
                logger.log(Level.INFO, "Login Failure:\n{0}", responseBody);
                return false;
            }
            return true;
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            return false;
        }
    }

    /*
     * Try connecting with credentials and session info which are stored in
     * cookies. If the cookies aren't present or are expired, this will fail.
     * If we can login, go ahead and fetch the vehicle list while we're at it.
     * @return  true    The connection based on stored cookies succeeded
     *          false   No dice, the user must supply credentials
     */
    public boolean connect() {
        vehicles.clear();
        if (!login()) return false;
        if (!fetchVehiclesInto(vehicles)) {
            // Delete the cookies file if it exists - it's not working
            File cf = new File(CookiesFile);
            if (cf.exists()) cf.delete();
            return false;
        }
        return true;
    }

    /*
     * Try connecting with the supplied credentials. If the login succeeds, the
     * credentials and session info will be stored in cookies for the future.
     * If we can login, go ahead and fetch the vehicle list while we're at it.
     * @return  true    The connection succeeded
     *          false   No dice, couldn't connect or fetch vehicles
     */
    public boolean connect(String username, String password, boolean remember) {
        vehicles.clear();
        if (!login(username, password)) return false;
        if (!fetchVehiclesInto(vehicles)) return false;
        
        // OK, we made it! Stash off the results of the successful login
        this.username = username;
        stashUsername();
        if (remember) CookieUtils.fetchAndWriteCookies(CookiesFile);
        return true;
    }

    
    //
    // Vehicle handling Methods
    //
    
    boolean fetchVehiclesInto(List<Vehicle> list) {
        try {
            JSONArray rawVehicleData = api.json(endpoint("vehicles")).array();
            int numVehicles = rawVehicleData.length();
            for (int i = 0; i < numVehicles; i++) {
                Vehicle vehicle = new Vehicle(this, rawVehicleData.getJSONObject(i));
                list.add(vehicle);
            }
        } catch (IOException | JSONException ex) {
            logger.log(Level.INFO, "Problem fetching vehicle list", ex);
            return false;
        }
        return true;
    }

    public List<Vehicle> getVehicles() { return vehicles; }

    
    
    //
    // Field Accessor Methods
    //
    
    public String getUsername() { return username; }    
    public RestyWrapper getAPI() { return api; }

}
