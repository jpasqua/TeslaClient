/*
 * REPL.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 6, 2013
 */

package org.noroomattheinn.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


/**
 * REPL: A Simple implementation of a Read / Evaluate / Print Loop based on
 * Handler objects.
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

public class REPL {
    // Protected Instance Variables
    protected String prompt;
    protected Map<String,Handler> handlerMap = new TreeMap<>();
        // Use a TreeMap to keep the handlers sorted for display
    protected Map<String,Handler> aliasMap = new HashMap<>();

    
    //
    // Constructors
    //
    
    public REPL(String prompt) {
        this.prompt = prompt;
        addHandler(new HelpHandler());
        addHandler(new ExitHandler());
    }
    
    
    //
    // Public methods
    //
    
    public void repl() {
        boolean keepGoing = true;
        
        while (keepGoing) {
            String input = CLUtils.getLine(prompt).toLowerCase();
            Handler h = handlerMap.get(input);
            if (h == null) h = aliasMap.get(input);
            if (h != null)
                keepGoing = h.execute();
            else {
                System.out.format("The value entered (%s) isn't a valid command\n", input);
                usage();
            }
        }
    }
    
    //
    // Protected methods
    //
    
    public final void addHandler(Handler h) {
        handlerMap.put(h.getHandlerName().toLowerCase(), h);
        String alias = h.getAlias();
        if (alias != null)
            aliasMap.put(alias.toLowerCase(), h);
    }

        
    //
    // Private Utility Methods
    //
    
    private void usage() {
        System.out.println("Commands:");
        for (Handler h : handlerMap.values()) {
            String name = h.getHandlerName();
            String desc = h.getDescription();
            String alias = h.getAlias();
            if (alias == null)  
                System.out.format("%s: %s\n", name, desc);
            else
                System.out.format("%s (%s): %s\n", name, alias, desc);
        }
    }
    
    
    //
    // Private Utility Classes
    //
    
    private class HelpHandler extends Handler {
        HelpHandler() { super("help", "display available commands", "?"); }
        public boolean execute() { usage(); return true; }
    }

    private class ExitHandler extends Handler {
        ExitHandler() { super("exit", "Exit this set of commands", "x"); }
        public boolean execute() { return false; }
    }

}

