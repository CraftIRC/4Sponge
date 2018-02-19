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
package org.kitteh.craftirc.util.loadable;

import ninja.leaping.configurate.ConfigurationNode;
import org.kitteh.craftirc.CraftIRC;
import org.kitteh.craftirc.exceptions.CraftIRCInvalidConfigException;

import javax.annotation.Nonnull;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Represents an object that can be loaded from config.
 */
public abstract class Loadable {
    /**
     * Defines a Loadable's information.
     */
    @Retention(value = RetentionPolicy.RUNTIME)
    public @interface Type {
        /**
         * Gets the name of the loadable.
         *
         * @return loadable name
         */
        String name();
    }

    /**
     * Loads data from config, if any is present.
     *
     * @param data data to load
     * @throws CraftIRCInvalidConfigException if invalid
     */
    protected abstract void load(@Nonnull CraftIRC plugin, @Nonnull ConfigurationNode data) throws CraftIRCInvalidConfigException;
}