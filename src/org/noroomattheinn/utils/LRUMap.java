/*
 * LRUMap.java - Copyright(c) 2014 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Apr 27, 2014
 */
package org.noroomattheinn.utils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Very simple non-concurrent LRU Map which can be used for caching. Lots of 
 * examples of this floating around on the net.
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */
public class LRUMap<K,V> extends LinkedHashMap<K,V> {
    private final int maxEntries;

    public LRUMap(final int maxEntries) {
        super(maxEntries + 1, 1.0f, true);
        this.maxEntries = maxEntries;
    }

    @Override
    protected boolean removeEldestEntry(final Map.Entry<K,V> eldest) {
        return super.size() > maxEntries;
    }
}
