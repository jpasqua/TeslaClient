/*
 * FlexEnum - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Dec 7, 2013
 */

package org.noroomattheinn.utils;

import java.util.ArrayList;
import java.util.Collection;

/**
 * FlexEnum: 
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */
public class FlexEnum<T> {
    private ArrayList<String> names = new ArrayList<>();
    private ArrayList<T> values = new ArrayList<>();
    private final String name;
    
    private FlexEnum(String name) {
        this.name = name;
    }
    
    public FlexEnum(String name, String[] keys, T[] vals) {
        this(name);
        if (keys.length != vals.length)
            throw new IllegalArgumentException("Unmatched number of keys/vals");
        for (int i = 0; i < keys.length; i++) {
            names.add(keys[i]);
            values.add(vals[i]);
        }
    }
    
    public FlexEnum<T> add(String key, T value) {
            names.add(key);
            values.add(value);
        return this;
    }
    
    public static <T>FlexEnum<T> build(String name, String key, T value) {
        FlexEnum<T> fe = new FlexEnum<>(name);
        return fe.add(key, value);
    }
    
    public T associatedValue(String key) {
        int index = names.indexOf(key);
        return values.get(index);
    }
    
    public String associatedKey(T value) {
        int index = values.indexOf(value);
        return names.get(index);
    }
    
    public Collection<String> allKeys() { return names; }
    public Collection<T> allValues() { return values; }
    
    @Override public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append(": [");
        boolean first = true;
        for (String key: allKeys()) {
            if (!first) {
                sb.append(", ");
            }
            T val = associatedValue(key);
            sb.append("(");
            sb.append(key);
            sb.append(", ");
            sb.append(val.toString());
            sb.append(")");
            first = false;
        }
        return sb.toString();
    }
    
    private enum TestEnum {A1, A2, A3};

    public static void main(String... args) {
        
        FlexEnum<String> fe1 = FlexEnum.
                <String>build("FE1", "key", "value").
                  add("key1", "v1").
                  add("k2", "v2").
                  add("k3", "v3");
        System.err.println(fe1);
        
        FlexEnum<TestEnum> fe2 = new FlexEnum<>("FE2", new String[] {"RA", "RB", "RC"}, TestEnum.values());
        System.err.println(fe2);
    }
}
