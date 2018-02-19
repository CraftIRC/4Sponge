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
package org.kitteh.craftirc.endpoint;

import ninja.leaping.configurate.ConfigurationNode;
import org.kitteh.craftirc.CraftIRC;
import org.kitteh.craftirc.endpoint.defaults.IRCEndpoint;
import org.kitteh.craftirc.endpoint.link.Link;
import org.kitteh.craftirc.exceptions.CraftIRCInvalidConfigException;
import org.kitteh.craftirc.util.loadable.LoadableTypeManager;
import org.kitteh.irc.client.library.util.Pair;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maintains {@link Endpoint}s and classes corresponding to Endpoint types.
 */
public final class EndpointManager extends LoadableTypeManager<Endpoint> {
    private final Map<String, Endpoint> endpoints = new ConcurrentHashMap<>();
    private final MessageDistributor messageDistributor;

    /**
     * Initialized by {@link CraftIRC} main.
     *
     * @param plugin the CraftIRC instance
     * @param endpoints a list of endpoint data to load
     */
    public EndpointManager(@Nonnull CraftIRC plugin, @Nonnull List<? extends ConfigurationNode> endpoints) {
        super(plugin, Endpoint.class);
        this.messageDistributor = new MessageDistributor(this, plugin);
        // We register ours first.
        this.registerType(IRCEndpoint.class);

        this.loadList(endpoints);
    }

    /**
     * Queues a message for delivery.
     *
     * @param message message to be sent
     */
    public void sendMessage(@Nonnull Message message) {
        this.messageDistributor.addMessage(message);
    }

    /**
     * Gets the Endpoint destinations of a named source Endpoint
     *
     * @param source source Endpoint
     * @return destinations of a message send by the speciified Endpoint
     */
    @Nonnull
    Set<Pair<Link, Endpoint>> getDestinations(@Nonnull String source) {
        Set<Pair<Link, Endpoint>> destinations = new HashSet<>();
        List<Link> links = this.getCraftIRC().getLinkManager().getLinks(source);
        for (Link link : links) {
            Endpoint endpoint = this.endpoints.get(link.getTarget());
            if (endpoint != null) {
                destinations.add(new Pair<>(link, endpoint));
            }
        }
        return destinations;
    }

    @Override
    protected void processCompleted(@Nonnull Endpoint endpoint) throws CraftIRCInvalidConfigException {
        final String name = endpoint.getName();
        if (this.endpoints.containsKey(name)) {
            throw new CraftIRCInvalidConfigException("Duplicate Endpoint name '" + name + "'");
        }
        this.endpoints.put(name, endpoint);
    }

    @Override
    protected void processFailedLoad(@Nonnull Exception exception, @Nonnull ConfigurationNode data) {
        CraftIRC.log().warning("Failed to load Endpoint", exception);
    }

    @Override
    protected void processInvalid(@Nonnull String reason, @Nonnull ConfigurationNode data) {
        CraftIRC.log().warning("Encountered invalid Endpoint: " + reason);
    }
}