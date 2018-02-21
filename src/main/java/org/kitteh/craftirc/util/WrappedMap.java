/*
 * * Copyright (C) 2014-2018 Matt Baxter http://kitteh.org
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.kitteh.craftirc.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * Wraps an existing map.
 * <p/>
 * The wrapped map is untouched, while an outer map stores any values which
 * have been changed, as well as any additional values.
 */
public class WrappedMap<Key, Value> {
    private final Map<Key, Value> innerMap;
    private final Map<Key, Value> outerMap = new HashMap<>();

    /**
     * Wraps a map.
     *
     * @param map map to be wrapped and untouched
     */
    public WrappedMap(@Nonnull Map<Key, Value> map) {
        this.innerMap = map;
    }

    /**
     * Gets the size of the map. Duplicated
     *
     * @return number of unique keys between both maps stored
     */
    public int size() {
        int size = this.innerMap.size();
        for (Key key : this.outerMap.keySet()) {
            if (!this.innerMap.containsKey(key)) {
                size++;
            }
        }
        return size;
    }

    /**
     * Gets if this map contains the specified key.
     *
     * @param key key which may exist in the map
     * @return true if the key is stored at either level
     */
    public boolean containsKey(@Nullable Key key) {
        return this.innerMap.containsKey(key) || this.outerMap.containsKey(key);
    }

    /**
     * Gets if this map contains the specified value. The value may be
     * located in the wrapped map but hidden by the outer map and in this
     * scenario will return false.
     *
     * @param value value which may exist in the map
     * @return true if the value is in the map and visible
     */
    public boolean containsValue(@Nullable Value value) {
        if (this.outerMap.containsValue(value)) {
            return true;
        }
        for (Map.Entry<Key, Value> entry : this.innerMap.entrySet()) {
            if (!this.outerMap.containsKey(entry.getKey())) {
                Value val = entry.getValue();
                if (value == null ? val == null : value.equals(val)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Gets the value mapped by the specified key.
     *
     * @param key the key
     * @return the value the key is mapped to, or null if no mapping exists
     */
    public Value get(@Nullable Key key) {
        if (this.outerMap.containsKey(key)) {
            return this.outerMap.get(key);
        }
        return this.innerMap.get(key);
    }

    /**
     * Maps a key to a value in the map.
     * <p/>
     * If the key exists in the modifiable, outer map, this method will
     * return that value. If the key only exists in the inner, wrapped map
     * this method will return the inner value which is now hidden by the
     * presence of the new mapping.
     *
     * @param key the key to map
     * @param value the value mapped to the key
     * @return the value 'displaced' by the new mapping (See above) or null
     * if nothing was displaced.
     */
    public Value put(@Nullable Key key, @Nullable Value value) {
        Value displaced;
        if (this.outerMap.containsKey(key)) {
            displaced = this.outerMap.get(key);
        } else {
            displaced = this.innerMap.get(key);
        }
        this.outerMap.put(key, value);
        return displaced;
    }

    /**
     * Removes a mapping from the modifiable map.
     *
     * @param key the key for which the mapping should be removed
     * @return the removed mapped value, or null if no mapping existed
     */
    public Value remove(@Nullable Key key) {
        return this.outerMap.remove(key);
    }

    /**
     * Deposits a pile of mappings to the modifiable map.
     *
     * @param m mappings to add to the modifiable map
     */
    public void putAll(@Nonnull Map<? extends Key, ? extends Value> m) {
        this.outerMap.putAll(m);
    }
}
