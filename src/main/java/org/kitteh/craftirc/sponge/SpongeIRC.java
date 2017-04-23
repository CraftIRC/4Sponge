/*
 * * Copyright (C) 2015 Matt Baxter http://kitteh.org
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
package org.kitteh.craftirc.sponge;

import com.google.inject.Inject;
import org.kitteh.craftirc.CraftIRC;
import org.kitteh.craftirc.endpoint.Endpoint;
import org.kitteh.craftirc.exceptions.CraftIRCUnableToStartException;
import org.kitteh.craftirc.sponge.util.Log4JWrapper;
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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Plugin(id = "craftirc", name = "CraftIRC", version = "4.1.5-SNAPSHOT", authors = "mbaxter",
        description = "Relay between IRC and Minecraft", url = "http://kitteh.org")
public class SpongeIRC {
    private static final String PERMISSION_RELOAD = "craftirc.reload";

    private CraftIRC craftIRC;
    @Inject
    @ConfigDir(sharedRoot = false)
    private File configDir;
    @Inject
    private Game game;
    @Inject
    private Logger logger;
    private Set<Endpoint> registeredEndpoints = new CopyOnWriteArraySet<>();
    private boolean reloading = false;

    @Listener
    public void init(@Nonnull GameInitializationEvent event) {
        this.startMeUp();
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
                    commandSource.sendMessage(Text.of(TextColors.AQUA, "CraftIRC version ", TextColors.WHITE, SpongeIRC.class.getAnnotation(Plugin.class).version(), TextColors.AQUA, " - Powered by Kittens"));
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
    Optional<CraftIRC> getCraftIRC() {
        return Optional.ofNullable(this.craftIRC);
    }

    @Nonnull
    Game getGame() {
        return this.game;
    }

    void registerEndpoint(Endpoint endpoint) {
        this.registeredEndpoints.add(endpoint);
        this.game.getEventManager().registerListeners(this, endpoint);
    }

    private synchronized void startMeUp() {
        try {
            this.craftIRC = new CraftIRC(new Log4JWrapper(this.logger), this.configDir);
        } catch (CraftIRCUnableToStartException e) {
            this.logger.error("Uh oh", e);
            return;
        }
        this.craftIRC.getFilterManager().registerArgumentProvider(SpongeIRC.class, () -> SpongeIRC.this);
        this.craftIRC.getFilterManager().registerType(PermissionFilter.class);
        this.craftIRC.getEndpointManager().registerArgumentProvider(SpongeIRC.class, () -> SpongeIRC.this);
        this.craftIRC.getEndpointManager().registerType(ChatEndpoint.class);
        this.craftIRC.getEndpointManager().registerType(JoinEndpoint.class);
        this.craftIRC.getEndpointManager().registerType(QuitEndpoint.class);
    }

    private synchronized void dontMakeAGrownManCry() {
        this.registeredEndpoints.forEach(endpoint -> this.game.getEventManager().unregisterListeners(endpoint));
        this.registeredEndpoints.clear();
        if (this.craftIRC != null) {
            this.craftIRC.shutdown();
        }
    }
}