/*
 * DefaultedHashMap.java - Copyright(c) 2014 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Nov 30, 2014
 */
package org.noroomattheinn.utils;

import java.util.HashMap;

/**
 * DefaultedHashMap: A HashMap that returns a default value instead of null
 * when performing a get() on a non-existent key.
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */
public class DefaultedHashMap<K, V> extends HashMap<K, V> {

    protected V defaultValue;

    public DefaultedHashMap(V defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override public V get(Object k) {
        return containsKey(Utils.<K>cast(k)) ? super.get(k) : defaultValue;
    }
}
