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
package org.kitteh.craftirc.irc;

import org.kitteh.craftirc.CraftIRC;
import org.kitteh.craftirc.endpoint.Endpoint;
import org.kitteh.craftirc.endpoint.Message;
import org.kitteh.craftirc.endpoint.defaults.IRCEndpoint;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.element.Channel;
import org.kitteh.irc.client.library.element.User;
import org.kitteh.irc.client.library.element.mode.ChannelUserMode;
import org.kitteh.irc.client.library.event.channel.ChannelCTCPEvent;
import org.kitteh.irc.client.library.event.channel.ChannelMessageEvent;
import org.kitteh.irc.client.library.util.CIKeyMap;
import org.kitteh.irc.lib.net.engio.mbassy.listener.Handler;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Wraps an IRC client and handles events.
 */
public final class IRCBot {
    private final Client client;
    private final String name;
    private final Map<String, Set<IRCEndpoint>> channels;
    private final CraftIRC plugin;

    IRCBot(@Nonnull CraftIRC plugin, @Nonnull String name, @Nonnull Client client) {
        this.plugin = plugin;
        this.client = client;
        this.channels = new CIKeyMap<>(client);
        this.name = name;
        this.client.getEventManager().registerEventListener(new Listener());
    }

    /**
     * Gets the bot's name.
     *
     * @return bot name
     */
    @Nonnull
    public String getName() {
        return this.name;
    }

    /**
     * Adds a channel to the bot, which will join when possible.
     *
     * @param endpoint endpoint this channel is assigned to
     * @param channel channel to join
     */
    public void addChannel(@Nonnull IRCEndpoint endpoint, @Nonnull String channel) {
        this.client.addChannel(channel);
        Set<IRCEndpoint> points = this.channels.computeIfAbsent(channel, k -> new CopyOnWriteArraySet<>());
        points.add(endpoint);
    }

    /**
     * Sends a message to the named channel.
     *
     * @param target target channel
     * @param message message to send
     */
    public void sendMessage(@Nonnull Channel target, @Nonnull String message) {
        this.client.sendMessage(target.getName(), message);
    }

    /**
     * Sends a message to the named target.
     *
     * @param target target
     * @param message message to send
     */
    public void sendMessage(@Nonnull String target, @Nonnull String message) {
        this.client.sendMessage(target, message);
    }

    void shutdown() {
        this.client.shutdown("CraftIRC!");
    }

    private void sendMessage(@Nonnull User sender, @Nonnull Channel channel, @Nonnull String message, @Nonnull IRCEndpoint.MessageType messageType) {
        final String channelName = channel.getName();
        if (!this.channels.containsKey(channelName)) {
            return;
        }
        Map<String, Object> data = new HashMap<>();
        data.put(IRCEndpoint.IRC_CHANNEL, channel.getName());
        data.put(IRCEndpoint.IRC_MASK, sender.getName());
        data.put(IRCEndpoint.IRC_MESSAGE_TYPE, messageType);
        StringBuilder modes = new StringBuilder();
        Optional<SortedSet<ChannelUserMode>> userModes = channel.getUserModes(sender);
        if (userModes.isPresent()) {
            for (ChannelUserMode mode : userModes.get()) {
                modes.append(mode.getNickPrefix());
            }
        }
        data.put(IRCEndpoint.IRC_PREFIX, (modes.length() == 0) ? "" : modes.charAt(0));
        data.put(IRCEndpoint.IRC_PREFIXES, modes.toString());
        data.put(IRCEndpoint.IRC_NICK, sender.getNick());
        data.put(Endpoint.MESSAGE_FORMAT, messageType.getFormat());
        data.put(Endpoint.MESSAGE_TEXT, message);
        data.put(Endpoint.SENDER_NAME, sender.getNick());
        String formatted = String.format(messageType.getFormat(), sender.getNick(), message);
        for (IRCEndpoint endpoint : this.channels.get(channelName)) {
            this.plugin.getEndpointManager().sendMessage(new Message(endpoint, formatted, data));
        }
    }

    private class Listener {
        @Handler
        public void message(@Nonnull ChannelMessageEvent event) {
            User user = event.getActor();
            IRCBot.this.sendMessage(user, event.getChannel(), event.getMessage(), IRCEndpoint.MessageType.MESSAGE);
        }

        @Handler
        public void action(@Nonnull ChannelCTCPEvent event) {
            if (event.getMessage().startsWith("ACTION ")) {
                IRCBot.this.sendMessage(event.getActor(), event.getChannel(), event.getMessage().substring("ACTION ".length()), IRCEndpoint.MessageType.ME);
            }
        }
    }
}