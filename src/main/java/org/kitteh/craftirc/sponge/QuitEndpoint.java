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

import org.kitteh.craftirc.endpoint.Endpoint;
import org.kitteh.craftirc.endpoint.Message;
import org.kitteh.craftirc.endpoint.TargetedMessage;
import org.kitteh.craftirc.util.MinecraftPlayer;
import org.kitteh.craftirc.util.loadable.Loadable;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The standard {@link Endpoint} for minecraft quit messages.
 */
@Loadable.Type(name = "mc-quit")
public class QuitEndpoint extends MinecraftEndpoint {
    public QuitEndpoint(@Nonnull SpongeIRC plugin) {
        super(plugin);
    }

    @Override
    protected void receiveMessage(@Nonnull TargetedMessage message) {
        // NOOP
    }

    @Listener
    public void onChat(@Nonnull ClientConnectionEvent.Disconnect event) {
        if (!event.getChannel().isPresent()) {
            return;
        }
        Map<String, Object> data = new HashMap<>();
        Set<MinecraftPlayer> recipients = this.collectionToMinecraftPlayer(event.getChannel().get().getMembers());
        data.put(QuitEndpoint.RECIPIENT_NAMES, recipients);
        this.getPlugin().getCraftIRC().getEndpointManager().sendMessage(new Message(this, event.getTargetEntity().getName() + " left the game", data));
    }
}