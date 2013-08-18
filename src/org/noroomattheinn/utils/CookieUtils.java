/*
 * CookieUtils.java - Copyright(c) 2013  All Rights Reserved, Joe Pasqua
 * Created: Jul 10, 2013
 */

package org.noroomattheinn.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CookieUtils: A set of utility methods for storing and retrieving cookies
 * persistently.
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public class CookieUtils {

    // Private Class Variables
    private static CookieStore cookieStore; 
        // If not null, this is the place to read/write cookies. If it is null,
        // then it means that there isn't an implementation registered or it's
        // of a subclass we don't understand and can't use

    static {    // Look up the CookieManager once and grab the CookieStore
        CookieHandler cHandler = CookieHandler.getDefault();
        if (cHandler != null) {
            if (cHandler instanceof CookieManager) {
                cookieStore = ((CookieManager) cHandler).getCookieStore();
            } else {
                CookieManager cm = new CookieManager();
                cm.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
                cookieStore = cm.getCookieStore();
                CookieHandler.setDefault(cm);
            }
        } else {
            cookieStore = null;
        }
    }
    

    //
    // Public Class Methods
    //
    
    
    /**
     * Read a set of cookies from a cookie file on disk and return them as a 
     * Set of HttpCookie objects. This method expects a file in the format that
     * is written by writeCookies()
     * 
     * @param cookieFileName    Where to red the cookies from
     * @return                  A Set<HttpCookie> corresponding to the cookies
     *                          read from cookieFileName
     */
    public static Set<HttpCookie> readCookies(String cookieFileName) {
        Set<HttpCookie> cookies = new HashSet<>();
        BufferedReader in = null;

        try {
            in = new BufferedReader(new FileReader(cookieFileName));
            String line;
            Pattern p = Pattern.compile(
                    "([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s+([^\\s]+)\\s+([^=]+)=([^;]+)\\s([^\\s]+)");
            //       Domain       PATH         Secure       MaxAge          Name=Val        Version
            Matcher m;
            while ((line = in.readLine()) != null) {
                m = p.matcher(line);
                if (m.matches()) {
                    int groupNum = 1;
                    String domain = m.group(groupNum++);
                    String path = m.group(groupNum++);
                    String secure = m.group(groupNum++);
                    String maxAge = m.group(groupNum++);
                    String name = m.group(groupNum++);
                    String val = m.group(groupNum++);
                    String version = m.group(groupNum++);
                    HttpCookie c = new HttpCookie(name, val);
                    c.setDomain(domain);
                    c.setSecure(Boolean.valueOf(secure));
                    c.setMaxAge(Integer.valueOf(maxAge));
                    c.setPath(path);
                    c.setVersion(Integer.valueOf(version));
                    cookies.add(c);
                }
            }
        } catch (IOException e) {
            Utils.logger.log(Level.FINEST, null, e);
        }
        if (in != null) try {
            in.close();
        } catch (IOException ex) {
            Utils.logger.log(Level.SEVERE, null, ex);
        }
        
        return cookies;
    }
    
    /**
     * Read all of the cookies in the file named cookieFileName and store them
     * in the CookieStore of the default CookieHandler.
     * @param cookieFileName    Where to read the cookies from
     * @return                  true if the cookies were successfully read and
     *                          stored, false otherwise
     */
    public static boolean readAndStoreCookies(String cookieFileName) {
        if (cookieStore == null)
            return false;   // No place to store the cookies!
        
        Set<HttpCookie>cookies = readCookies(cookieFileName);
        if (cookies == null || cookies.isEmpty())
            return false;   // No cookies to store
                                
        // Now that we have the cookies, put them in the cookie jar
        for (HttpCookie c : cookies)
            cookieStore.add(URI.create(c.getDomain()), c);

        return true;
    }

    /**
     * Add a cookie to the CookieStore with the given URI.
     * @param c     The cookie to be added
     * @param uri   The URI to associate with the cookie. If the URI is null
     *              then we use the cookie's domain
     */
    public static void addCookie(HttpCookie c, URI uri) {
        if (cookieStore == null) return;
        String domain = c.getDomain();
        if (domain == null || domain.isEmpty())
            domain = "local";
        try {
            c.setDomain(domain);
        } catch (Exception e) {
            System.err.println(e);
        }
        if (uri == null)
            uri = URI.create(domain);
        cookieStore.add(uri, c);
    }

    
    /**
     * Fetch a named cookie from the CookieStore. The cookie must be
     * associated with the specified URI
     * @param name  The name of the cookie
     * @param uri   The URI associated with the cookie
     * @return      The requested HttpCookie or null if a matching
     *              cookie can't be found
     */
    public static HttpCookie getCookie(String name, URI uri) {
        if (cookieStore == null) return null;
        List<HttpCookie> cookies = cookieStore.get(uri);
        for (HttpCookie c : cookies) {
            if (c.getName().equals(name))
                return c;
        }
        return null;
    }
    
    /**
     * Write the specified list of cookies to a file with the given name.
     * 
     * @param cookieFileName    Where to write the cookies
     * @param cookies           The cookies themselves
     * @return                  true if successfully written, false otherwise
     */
    public static boolean writeCookies(String cookieFileName, List<HttpCookie> cookies) {
        File f = new File(cookieFileName);
//        Always write the cookies out because things may have changed since last time        
//        if (f.exists())
//            return false;
        try (PrintStream out = new PrintStream(new FileOutputStream(cookieFileName))) {
            for (HttpCookie c : cookies) {
                out.println(
                        c.getDomain() + "\t" +  // The Domain
                        c.getPath() + "\t" +    // The Path
                        c.getSecure() + "\t" +  // Must the connection be secure
                        c.getMaxAge() + "\t" +  // How long to keep the cookie
                        c.getName()+"="+c.getValue() + "\t" +
                        c.getVersion());
            }
        } catch (IOException ex) {
            Utils.logger.log(Level.FINEST, null, ex);
            return false;
        }
        return true;
    }
    
    /**
     * Fetch all of the cookies form the CookieStore and write them to disk
     * @param cookieFileName    Where to write the cookies
     * @return                  true if the cookies were fetched and written
     *                          successfully, false otherwise
     */
    public static boolean fetchAndWriteCookies(String cookieFileName) {
        if (cookieStore == null)
            return false;   // No place to store the cookies!
        
        // Fetch the cookies from the cookie jar
        List<HttpCookie> cookies = cookieStore.getCookies();

        if (cookies == null || cookies.isEmpty())
            return false;
        
        // OK, now that we've got the cookies, write them to a file
        boolean written = writeCookies(cookieFileName, cookies);
        return written;
    }
    

}

