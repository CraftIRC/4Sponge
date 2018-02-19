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
package org.kitteh.craftirc.endpoint.link;

import ninja.leaping.configurate.ConfigurationNode;
import org.kitteh.craftirc.CraftIRC;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Maintains {@link Link}s and classes corresponding to Link types.
 */
public final class LinkManager {
    private final Map<String, List<Link>> links = new ConcurrentHashMap<>();

    /**
     * Initialized by {@link CraftIRC} main.
     *
     * @param plugin the CraftIRC instance
     * @param links a list of link data to load
     */
    public LinkManager(@Nonnull CraftIRC plugin, @Nonnull List<? extends ConfigurationNode> links) {
        int nonMap = 0;
        int noSource = 0;
        int noTarget = 0;
        for (final ConfigurationNode node : links) {
            if (!node.hasMapChildren()) {
                nonMap++;
                continue;
            }
            final String source = node.getNode("source").getString();
            if (source == null) {
                noSource++;
                continue;
            }
            final String target = node.getNode("target").getString();
            if (target == null) {
                noTarget++;
                continue;
            }
            List<? extends ConfigurationNode> filters = node.getNode("filters").getChildrenList();
            this.addLink(new Link(plugin, source, target, filters));
        }
        if (nonMap > 0) {
            CraftIRC.log().warning(String.format("Links list contained %d entries which were not maps", nonMap));
        }
        if (noSource > 0) {
            CraftIRC.log().warning(String.format("Links list contained %d entries without a source specified", noSource));
        }
        if (noTarget > 0) {
            CraftIRC.log().warning(String.format("Links list contained %d entries without a target specified", noTarget));
        }
        if (this.links.isEmpty()) {
            CraftIRC.log().severe("Loaded no links! Nothing will be passed between any Endpoints!");
        }
    }

    @Nonnull
    public List<Link> getLinks(@Nonnull String source) {
        LinkedList<Link> linkList = new LinkedList<>();
        List<Link> links = this.links.get(source);
        if (links != null) {
            linkList.addAll(links);
        }
        return linkList;
    }

    private void addLink(@Nonnull Link link) {
        List<Link> links = this.links.computeIfAbsent(link.getSource(), k -> new LinkedList<>());
        links.add(link);
    }
}