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
package org.kitteh.craftirc;

import com.google.inject.Inject;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.kitteh.craftirc.endpoint.Endpoint;
import org.kitteh.craftirc.endpoint.EndpointManager;
import org.kitteh.craftirc.endpoint.filter.FilterManager;
import org.kitteh.craftirc.endpoint.link.LinkManager;
import org.kitteh.craftirc.exceptions.CraftIRCInvalidConfigException;
import org.kitteh.craftirc.exceptions.CraftIRCUnableToStartException;
import org.kitteh.craftirc.exceptions.CraftIRCWillLeakTearsException;
import org.kitteh.craftirc.irc.BotManager;
import org.kitteh.craftirc.sponge.ChatEndpoint;
import org.kitteh.craftirc.sponge.JoinEndpoint;
import org.kitteh.craftirc.sponge.PermissionFilter;
import org.kitteh.craftirc.sponge.QuitEndpoint;
import org.kitteh.craftirc.util.shutdownable.Shutdownable;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartingServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;


@Plugin(id = "craftirc", name = "CraftIRC", version = "4.2.0", authors = "mbaxter",
        description = "Relay between IRC and Minecraft", url = "http://kitteh.org")
public final class CraftIRC {
    private static Logger loggy;
    private static final String PERMISSION_RELOAD = "craftirc.reload";

    @Inject
    @ConfigDir(sharedRoot = false)
    private File configDir;
    @Inject
    private Game game;
    @Inject
    private Logger logger;
    private final Set<Endpoint> registeredEndpoints = new CopyOnWriteArraySet<>();
    private boolean reloading = false;
    private String version = CraftIRC.class.getAnnotation(Plugin.class).version();

    @Listener
    public void init(@Nonnull GameInitializationEvent event) {
        this.startMeUp();
        System.out.println("Hello again");
    }

    @Listener
    public void starting(@Nonnull GameStartingServerEvent event) {
        CommandSpec reloadSpec = CommandSpec.builder()
                .executor((commandSource, commandContext) -> {
                    if (this.reloading) {
                        commandSource.sendMessage(Text.of(TextColors.RED, "CraftIRC reload already in progress"));
                    } else {
                        this.reloading = true;
                        commandSource.sendMessage(Text.of(TextColors.AQUA, "CraftIRC reload scheduled"));
                        this.game.getScheduler().createTaskBuilder()
                                .async()
                                .execute(() -> {
                                    this.dontMakeAGrownManCry();
                                    this.startMeUp();
                                    this.reloading = false;
                                })
                                .name("CraftIRC Reloading...")
                                .submit(this);
                    }
                    return CommandResult.success();
                })
                .permission(PERMISSION_RELOAD)
                .build();
        CommandSpec mainSpec = CommandSpec.builder()
                .child(reloadSpec, "reload")
                .executor((commandSource, commandContext) -> {
                    commandSource.sendMessage(Text.of(TextColors.AQUA, "CraftIRC version ", TextColors.WHITE, this.version, TextColors.AQUA, " - Powered by Kittens"));
                    return CommandResult.success();
                })
                .build();
        this.game.getCommandManager().register(this, mainSpec, "craftirc");
    }

    @Listener
    public void stahp(@Nonnull GameStoppingEvent event) {
        this.dontMakeAGrownManCry();
    }

    @Nonnull
    public Game getGame() {
        return this.game;
    }

    public void registerEndpoint(Endpoint endpoint) {
        this.registeredEndpoints.add(endpoint);
        this.game.getEventManager().registerListeners(this, endpoint);
    }

    private synchronized void startMeUp() {
        try {
            CraftIRC.loggy = logger;

            File configFile = new File(this.configDir, "config.yml");
            if (!configFile.exists()) {
                log().info("No config.yml found, creating a default configuration.");
                this.saveDefaultConfig(this.configDir);
            }

            YAMLConfigurationLoader yamlConfigurationLoader = YAMLConfigurationLoader.builder().setPath(configFile.toPath()).build();
            ConfigurationNode root = yamlConfigurationLoader.load();

            if (root.isVirtual()) {
                throw new CraftIRCInvalidConfigException("Config doesn't appear valid. Would advise starting from scratch.");
            }

            ConfigurationNode repeatableFilters = root.getNode("repeatable-filters");

            ConfigurationNode botsNode = root.getNode("bots");
            List<? extends ConfigurationNode> bots;
            if (botsNode.isVirtual() || (bots = botsNode.getChildrenList()).isEmpty()) {
                throw new CraftIRCInvalidConfigException("No bots defined!");
            }

            ConfigurationNode endpointsNode = root.getNode("endpoints");
            List<? extends ConfigurationNode> endpoints;
            if (endpointsNode.isVirtual() || (endpoints = endpointsNode.getChildrenList()).isEmpty()) {
                throw new CraftIRCInvalidConfigException("No endpoints defined! Would advise starting from scratch.");
            }

            ConfigurationNode linksNode = root.getNode("links");
            List<? extends ConfigurationNode> links;
            if (linksNode.isVirtual() || (links = linksNode.getChildrenList()).isEmpty()) {
                throw new CraftIRCInvalidConfigException("No links defined! How can your endpoints be useful?");
            }

            this.filterManager = new FilterManager(this, repeatableFilters);
            this.botManager = new BotManager(this, bots);
            this.endpointManager = new EndpointManager(this, endpoints);
            this.linkManager = new LinkManager(this, links);
        } catch (Exception e) {
            this.logger.error("Uh oh", new CraftIRCUnableToStartException("Could not start CraftIRC!", e));
        }
        this.getFilterManager().registerArgumentProvider(CraftIRC.class, () -> CraftIRC.this);
        this.getFilterManager().registerType(PermissionFilter.class);
        this.getEndpointManager().registerArgumentProvider(CraftIRC.class, () -> CraftIRC.this);
        this.getEndpointManager().registerType(ChatEndpoint.class);
        this.getEndpointManager().registerType(JoinEndpoint.class);
        this.getEndpointManager().registerType(QuitEndpoint.class);
    }

    private synchronized void dontMakeAGrownManCry() {
        this.registeredEndpoints.forEach(endpoint -> this.game.getEventManager().unregisterListeners(endpoint));
        this.registeredEndpoints.clear();
        this.shutdownables.forEach(Shutdownable::shutdown);
        // And lastly...
        CraftIRC.loggy = null;
    }

    @Nonnull
    public static Logger log() {
        if (CraftIRC.loggy == null) {
            throw new CraftIRCWillLeakTearsException();
        }
        return CraftIRC.loggy;
    }

    private BotManager botManager;
    private EndpointManager endpointManager;
    private FilterManager filterManager;
    private LinkManager linkManager;
    private final Set<Shutdownable> shutdownables = new CopyOnWriteArraySet<>();

    @Nonnull
    public BotManager getBotManager() {
        return this.botManager;
    }

    @Nonnull
    public EndpointManager getEndpointManager() {
        return this.endpointManager;
    }

    @Nonnull
    public FilterManager getFilterManager() {
        return this.filterManager;
    }

    @Nonnull
    public LinkManager getLinkManager() {
        return this.linkManager;
    }

    /**
     * Starts tracking a feature which can be shut down.
     *
     * @param shutdownable feature to track
     */
    public void trackShutdownable(@Nonnull Shutdownable shutdownable) {
        this.shutdownables.add(shutdownable);
    }

    private void saveDefaultConfig(@Nonnull File dataFolder) {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        try {
            URL url = this.getClass().getClassLoader().getResource("config.yml");
            if (url == null) {
                log().warn("Could not find a default config to copy!");
                return;
            }

            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            InputStream input = connection.getInputStream();

            File outFile = new File(dataFolder, "config.yml");
            OutputStream output = new FileOutputStream(outFile);

            byte[] buffer = new byte[1024];
            int lengthRead;
            while ((lengthRead = input.read(buffer)) > 0) {
                output.write(buffer, 0, lengthRead);
            }

            output.close();
            input.close();
        } catch (IOException ex) {
            log().error("Exception while saving default config", ex);
        }
    }
}