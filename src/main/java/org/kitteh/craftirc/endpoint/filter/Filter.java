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
package org.kitteh.craftirc.endpoint.filter;

import ninja.leaping.configurate.ConfigurationNode;
import org.kitteh.craftirc.CraftIRC;
import org.kitteh.craftirc.endpoint.TargetedMessage;
import org.kitteh.craftirc.endpoint.link.Link;
import org.kitteh.craftirc.exceptions.CraftIRCInvalidConfigException;
import org.kitteh.craftirc.util.loadable.Loadable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This is a filter.
 */
public abstract class Filter extends Loadable {
    private Link link;
    private Link.LinkFilterLoader loader;

    /**
     * Gets the Link using this Filter instance.
     *
     * @return the Link in use
     */
    @Nonnull
    protected Link getLink() {
        return this.link;
    }

    @Nullable
    Link.LinkFilterLoader getLoader() {
        return this.loader;
    }

    /**
     * Processes an incoming message. Should be capable of handling calls
     * from multiple threads at once.
     *
     * @param message message to process
     */
    public abstract void processMessage(@Nonnull TargetedMessage message);

    @Override
    protected final void load(@Nonnull CraftIRC plugin, @Nonnull ConfigurationNode data) throws CraftIRCInvalidConfigException {
        if (!data.getNode(FilterManager.Target.EndpointLoader).isVirtual()) {
            this.loader = (Link.LinkFilterLoader) data.getNode(FilterManager.Target.EndpointLoader).getValue();
            this.link = this.loader.getLink();
        }
        this.load(data);
    }

    /**
     * Loads this filter's data.
     *
     * @param data information to load
     * @throws CraftIRCInvalidConfigException if things go poorly
     */
    protected void load(@Nonnull ConfigurationNode data) throws CraftIRCInvalidConfigException {

    }
}
