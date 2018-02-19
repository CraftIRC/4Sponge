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

import ninja.leaping.configurate.ConfigurationNode;
import org.kitteh.craftirc.CraftIRC;
import org.kitteh.irc.client.library.Client;
import org.kitteh.irc.client.library.feature.auth.NickServ;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages IRC bots.
 */
public final class BotManager {
    private final Map<String, IRCBot> bots = new ConcurrentHashMap<>();
    private final CraftIRC plugin;

    /**
     * Initialized by {@link CraftIRC} main.
     *
     * @param plugin the CraftIRC instance
     * @param bots list of bot data to load
     */
    public BotManager(@Nonnull CraftIRC plugin, @Nonnull List<? extends ConfigurationNode> bots) {
        this.plugin = plugin;
        this.plugin.trackShutdownable(() -> BotManager.this.bots.values().forEach(IRCBot::shutdown));
        this.loadBots(bots);
    }

    /**
     * Gets a bot by name.
     *
     * @param name bot name
     * @return named bot or null if no such bot exists
     */
    @Nullable
    public IRCBot getBot(@Nonnull String name) {
        return this.bots.get(name);
    }

    private void loadBots(@Nonnull List<? extends ConfigurationNode> list) {
        Set<String> usedBotNames = new HashSet<>();
        int nonMap = 0;
        int noName = 0;
        for (final ConfigurationNode node : list) {
            if (!node.hasMapChildren()) {
                nonMap++;
                continue;
            }
            final String name = node.getNode("name").getString();
            if (name == null) {
                noName++;
                continue;
            }
            if (!usedBotNames.add(name)) {
                CraftIRC.log().warning(String.format("Ignoring duplicate bot with name %s", name));
                continue;
            }
            this.addBot(name, node);
        }
        if (nonMap > 0) {
            CraftIRC.log().warning(String.format("Bots list contained %d entries which were not maps", nonMap));
        }
        if (noName > 0) {
            CraftIRC.log().warning(String.format("Bots list contained %d entries without a 'name'", noName));
        }
    }

    private void addBot(@Nonnull String name, @Nonnull ConfigurationNode data) {
        Client.Builder botBuilder = Client.builder();
        botBuilder.name(name);
        botBuilder.serverHost(data.getNode("host").getString("localhost"));
        botBuilder.serverPort(data.getNode("port").getInt(6667));
        botBuilder.secure(data.getNode("ssl").getBoolean());
        ConfigurationNode password = data.getNode("password");
        if (!password.isVirtual()) {
            botBuilder.serverPassword(password.getString());
        }
        botBuilder.user(data.getNode("user").getString("CraftIRC"));
        botBuilder.realName(data.getNode("realname").getString("CraftIRC Bot"));

        ConfigurationNode bind = data.getNode("bind");
        ConfigurationNode bindHost = bind.getNode("host");
        if (!bindHost.isVirtual()) {
            botBuilder.bindHost(bindHost.getString());
        }
        botBuilder.bindPort(bind.getNode("port").getInt(0));
        botBuilder.nick(data.getNode("nick").getString("CraftIRC"));

        ConfigurationNode auth = data.getNode("auth");
        String authUser = auth.getNode("user").getString();
        String authPass = auth.getNode("pass").getString();
        boolean nickless = auth.getNode("nickless").getBoolean();

        ConfigurationNode debug = data.getNode("debug-output");
        if (debug.getNode("exceptions").getBoolean()) {
            botBuilder.exceptionListener(exception -> CraftIRC.log().warning("Exception on bot " + name, exception));
        } else {
            botBuilder.exceptionListener(null);
        }
        if (debug.getNode("input").getBoolean()) {
            botBuilder.inputListener(input -> CraftIRC.log().info("[IN] " + input));
        }
        if (debug.getNode("output").getBoolean()) {
            botBuilder.outputListener(output -> CraftIRC.log().info("[OUT] " + output));
        }

        Client newBot = botBuilder.build();

        if (authUser != null && authPass != null) {
            newBot.getAuthManager().addProtocol(nickless ? new NicklessServ(newBot, authUser, authPass) : new NickServ(newBot, authUser, authPass));
        }

        this.bots.put(name, new IRCBot(this.plugin, name, newBot));
    }
}