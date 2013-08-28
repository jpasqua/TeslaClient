/*
 * Handler.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Jul 5, 2013
 */
package org.noroomattheinn.utils;

/**
 * Handler: A very simple abstract class that represents functionality to handle
 * some event or activity. A Handler is represented by a name and optional alias.
 * It has a short description of what it does. Finally, it has an execute method
 * that implements whatever functionality the handler makes available.
 * 
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */
public abstract class Handler {
    // Private Instance Variables
    private String name;
    private String description;
    private String alias;
    
    
    //
    // Public Default Method Implemtations
    //
    
    public String getHandlerName() { return name; }
    public String getDescription() { return description; }
    public String getAlias() { return alias; }
    
    //
    // Abstract Methods
    //
    
    abstract public boolean execute();
    
    
    //
    // Constructors
    //
    
    public Handler(String name, String description, String alias) {
        this.name = name;
        this.description = description;
        this.alias = alias;
    }
    
    public Handler(String name, String description) {
        this(name, description, null);
    }
    
}
