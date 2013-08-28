/*
 * CLUtils.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 10, 2013
 */

package org.noroomattheinn.utils;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 * CLUtils: A collection of methods designed to aid in writing simple command-line
 * based tools.
 *
 * TO DO: There should be a method to set a format string for the prompt used
 * by the various input methods.
 * 
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public class CLUtils {

    // Private Class Variables
    private static final BufferedReader reader = 
            new BufferedReader(new InputStreamReader(System.in));
    private static Console console = System.console();
    private static List<String> available = new ArrayList<>();
    
    
    //
    // Public Class Methods
    //
    
    /**
     * Ask the user to enter some data
     * 
     * @param prompt    A string to display to the user when asking for input
     * @return          The string typed by the user. This may be empty
     */
    public static String getLine(String prompt) {
        if (!available.isEmpty()) {
            return(available.remove(0));
        }
        
        System.out.format("[%s]: ", prompt);
        try {
            String line = reader.readLine().trim();
            String[] lines = StringUtils.split(line, ';');
            for (int i = 0; i < lines.length; i++) {
                available.add(lines[i]);
            }
            return getLine(prompt);
        } catch (IOException ex) {
            return "";
        }
    }

    /**
     * Ask the user to enter a password. If at all possible, do this in a way
     * that will not echo the password to the terminal. The Console class has
     * a way to do that, but not all contexts make the Console available. For
     * example, when running in the debugger, the Console may be null.
     * 
     * @return  The password entered by the user
     */
    public static String getPassword() {
        if (console == null) {
            return getLine("Password");
        }
        return new String(console.readPassword("Password: "));
    }

    
    /**
     * Ask the user to choose an option by typing its name. The options are 
     * specified by an array of Objects. Their names are derived by toSting().
     * An array of Stings or an array of enum values are obvious choices. This
     * method won't return until a valid choice is selected. If an invalid 
     * choice is entered, the user will be shown the valid choices and asked
     * to choose again.
     * @param prompt    What to display to the user to prompt input
     * @param choices   An array of objects representing the choices
     * @return          A string corresponding to the selected choice
     */
    public static String chooseOption(String prompt, Object[] choices) {
        String input;
        String allChoices = StringUtils.join(choices, ", ");
        
        while (true) {
            input = getLine(prompt);
            for (Object o : choices) {
                if (o.toString().equalsIgnoreCase(input)) {
                    return o.toString();
                }
            }
            System.err.println("Please select one of these options: " + allChoices);
        }
    }

    /**
     * Ask the user to enter a number within a range. This
     * method won't return until a valid number is entered. If an invalid 
     * choice is entered, the user will be shown the valid range and asked
     * to choose again.
     * @param prompt    What to display to the user to prompt input
     * @param min       The minimum allowable value
     * @param max       The maximum allowable value
     * @return          A double corresponding to the number entered
     */
    public static double getNumberInRange(String prompt, double min, double max) {
        while (true) {
            String input = "";
            try {
                input = getLine(prompt);
                double val = Double.valueOf(input);
                if (val >= min && val <= max) {
                    return val;
                }
            } catch (NumberFormatException e) {
                System.err.println(input + " is not a number");
            }
            System.err.format("Please enter a value from %f to %f\n", min, max);
        }
    }
    
}
