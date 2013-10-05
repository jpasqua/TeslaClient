/*
 * CircularBuffer.java - Copyright(c) 2013 Joe Pasqua
 * Provided under the MIT License. See the LICENSE file for details.
 * Created: Sep 28, 2013
 * 
 * NOTE: This code is based on the example shown here:
 *     http://bradforj287.blogspot.com/2010/11/efficient-circular-buffer-in-java.html
 * Rights for that code are "feel free to use it and do whatever you'd like with it."
 * 
 */

package org.noroomattheinn.utils;

import java.util.NoSuchElementException;

/**
 * CircularBuffer: Thread-safe circular buffer backed by an array. It is
 * possible to peek at the any element in the buffer, not just the first
 * and last.
 *
 * @author Joe Pasqua <joe at NoRoomAtTheInn dot org>
 */

/**
 * CircularBuffer backed by an array. The operations are thread-safe.
 *
 */
public class CircularBuffer<T> {

/*------------------------------------------------------------------------------
 *
 * Internal State
 * 
 *----------------------------------------------------------------------------*/

    private T[] data;               // The data elements
    private int front = 0;          // The oldest element
    private int insertLocation = 0; // Where to put the next piece of data
    private int size = 0;           // The number of elements in queue, not the capacity
    
    
/*==============================================================================
 * -------                                                               -------
 * -------              Public Interface To This Class                   ------- 
 * -------                                                               -------
 *============================================================================*/

    /**
     * Creates a circular buffer with the specified size.
     *
     * @param bufferSize - the maximum size of the buffer
     */
    public CircularBuffer(int bufferSize) {
        data = Utils.cast(new Object[bufferSize]);
    }

    /**
     * Inserts an item at the end of the queue. If the queue is full, the oldest
     * value will be removed and head of the queue will become the second oldest
     * value.
     *
     * @param item - the item to be inserted
     */
    public synchronized void insert(T item) {
        data[insertLocation] = item;
        insertLocation = (insertLocation + 1) % data.length;

        // If the queue is full, this means we just overwrote the front of the
        // queue. So increment the front location.
        if (size == data.length) {
            front = (front + 1) % data.length;
        } else {
            size++;
        }
    }

    /**
     * Returns the number of elements in the buffer (not the capacity)
     *
     * @return int - The number of elements inside this buffer
     */
    public synchronized int size() { return size; }

    /**
     * Returns the head element of the queue.
     *
     * @return The head element of the queue
     */
    public synchronized T removeFront() {
        if (size == 0) {
            throw new NoSuchElementException();
        }
        T retValue = data[front];
        front = (front + 1) % data.length;
        size--;
        return retValue;
    }

    /**
     * Returns the head of the queue but does not remove it.
     *
     * @return  The head element of the queue
     */
    public synchronized T peekFront() {
        if (size == 0) {
            return null;
        } else {
            return data[front];
        }
    }

    /**
     * Returns the nth element of the queue but does not remove it.
     *
     * @return T - The nth element
     */
    public synchronized T peekAt(int n) {
        if (size <= n) {
            return null;
        } else {
            return data[(front + n) % data.length];
        }
    }

    /**
     * Returns the last element of the queue but does not remove it.
     *
     * @return T - The most recently added value
     */
    public synchronized T peekLast() {
        if (size == 0) {
            return null;
        } else {
            int lastElement = insertLocation - 1;
            if (lastElement < 0) {
                lastElement = data.length - 1;
            }
            return data[lastElement];
        }
    }
}