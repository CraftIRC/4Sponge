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
package org.kitteh.craftirc.sponge;

import org.kitteh.craftirc.CraftIRC;
import org.kitteh.craftirc.endpoint.Endpoint;
import org.kitteh.craftirc.endpoint.Message;
import org.kitteh.craftirc.endpoint.TargetedMessage;
import org.kitteh.craftirc.util.MinecraftPlayer;
import org.kitteh.craftirc.util.loadable.Loadable;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The standard {@link Endpoint} for minecraft join messages.
 */
@Loadable.Type(name = "mc-join")
public class JoinEndpoint extends MinecraftEndpoint {
    public JoinEndpoint(@Nonnull CraftIRC plugin) {
        super(plugin);
    }

    @Override
    protected void receiveMessage(@Nonnull TargetedMessage message) {
        // NOOP
    }

    @Listener
    public void onChat(@Nonnull ClientConnectionEvent.Join event) {
        if (!event.getChannel().isPresent()) {
            return;
        }
        Map<String, Object> data = new HashMap<>();
        Set<MinecraftPlayer> recipients = this.collectionToMinecraftPlayer(event.getChannel().get().getMembers());
        data.put(JoinEndpoint.SENDER_NAME, event.getCause().first(Player.class).get().getName());
        data.put(JoinEndpoint.RECIPIENT_NAMES, recipients);
        this.getPlugin().getEndpointManager().sendMessage(new Message(this, event.getTargetEntity().getName() + " joined the game", data));
    }
}
