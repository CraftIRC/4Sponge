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
import org.kitteh.craftirc.exceptions.CraftIRCUnableToStartException;
import org.kitteh.craftirc.sponge.util.Log4JWrapper;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;

import javax.annotation.Nonnull;
import java.io.File;

@Plugin(id = "CraftIRC", name = "CraftIRC", version = SpongeIRC.MAGIC_VERSION)
public class SpongeIRC {
    // Field set by javassist-maven-plugin, confirmed by unit test
    static final String MAGIC_VERSION = "SET_BY_MAGIC"; // KEEP THIS VALUE EQUAL TO SET_BY_MAGIC FOR UNIT TEST

    private CraftIRC craftIRC;
    @Inject
    @ConfigDir(sharedRoot = false)
    private File configDir;
    @Inject
    private Game game;
    @Inject
    private Logger logger;

    @Listener
    public void init(@Nonnull GameInitializationEvent event) {
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

    @Nonnull
    CraftIRC getCraftIRC() {
        return this.craftIRC;
    }

    @Nonnull
    Game getGame() {
        return this.game;
    }

    // TODO shutdown - https://github.com/SpongePowered/SpongeAPI/issues/442
}