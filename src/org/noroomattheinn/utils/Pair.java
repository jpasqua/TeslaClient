/*
 * Pair.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Sep 28, 2013
 */

package org.noroomattheinn.utils;

/**
 * Pair: a 2-tuple
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */
public class Pair<T1, T2> {

    public final T1 item1;
    public final T2 item2;

    public Pair(T1 k, T2 v) {
        this.item1 = k;
        this.item2 = v;
    }
}
