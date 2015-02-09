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
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.event.state.InitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.service.config.ConfigDir;
import org.spongepowered.api.util.event.Subscribe;

import java.io.File;

@Plugin(id = "CraftIRC", name = "CraftIRC", version = SpongeIRC.MAGIC_VERSION)
public class SpongeIRC {
    static final String MAGIC_VERSION = "SET_BY_MAGIC";

    private CraftIRC craftIRC;
    @Inject
    @ConfigDir(sharedRoot = false)
    private File configDir;
    @Inject
    private Game game;
    @Inject
    private Logger logger;

    @Subscribe
    public void init(InitializationEvent event) {
        try {
            this.craftIRC = new CraftIRC(new org.kitteh.craftirc.util.Logger() {
                @Override
                public void info(String info) {
                    SpongeIRC.this.logger.info(info);
                }

                @Override
                public void warning(String warn) {
                    SpongeIRC.this.logger.warn(warn);
                }

                @Override
                public void warning(String warn, Throwable thrown) {
                    SpongeIRC.this.logger.warn(warn, thrown);
                }

                @Override
                public void severe(String severe) {
                    SpongeIRC.this.logger.error(severe);
                }

                @Override
                public void severe(String severe, Throwable thrown) {
                    SpongeIRC.this.logger.error(severe, thrown);
                }
            }, this.configDir);
        } catch (CraftIRCUnableToStartException e) {
            this.logger.error("Uh oh", e);
        }
        this.craftIRC.getEndpointManager().registerArgumentProvider(SpongeIRC.class, () -> SpongeIRC.this);
        this.craftIRC.getEndpointManager().registerType(MinecraftEndpoint.class);
    }

    CraftIRC getCraftIRC() {
        return this.craftIRC;
    }

    Game getGame() {
        return this.game;
    }
}