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

import com.google.common.base.Optional;
import org.kitteh.craftirc.CraftIRC;
import org.kitteh.craftirc.endpoint.Endpoint;
import org.kitteh.craftirc.endpoint.Message;
import org.kitteh.craftirc.endpoint.TargetedMessage;
import org.kitteh.craftirc.util.MinecraftPlayer;
import org.kitteh.craftirc.util.loadable.Loadable;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.entity.living.player.PlayerChatEvent;
import org.spongepowered.api.util.event.Subscribe;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The standard {@link org.kitteh.craftirc.endpoint.Endpoint} for minecraft
 * chat messages.
 */
@Loadable.Type(name = "minecraft")
public class MinecraftEndpoint extends Endpoint {
    public static final String PLAYER_LIST = "RECIPIENT_NAMES";

    private final SpongeIRC plugin;

    public MinecraftEndpoint(SpongeIRC plugin) {
        this.plugin = plugin;
        this.plugin.getGame().getEventManager().register(plugin, this);
    }

    @Override
    protected void preProcessReceivedMessage(TargetedMessage message) {
        List<MinecraftPlayer> players = this.plugin.getGame().getServer().get().getOnlinePlayers().stream().map(player -> new MinecraftPlayer(player.getName(), player.getUniqueId())).collect(Collectors.toCollection(LinkedList::new));
        message.getCustomData().put(MinecraftEndpoint.PLAYER_LIST, players);
    }

    @Override
    protected void receiveMessage(TargetedMessage message) {
        @SuppressWarnings("unchecked")
        List<MinecraftPlayer> recipients = (List<MinecraftPlayer>) message.getCustomData().get(MinecraftEndpoint.PLAYER_LIST);
        /* TODO wait for recipient tracking
        for (MinecraftPlayer recipient : recipients) {
            Optional<Player> player = this.plugin.getGame().getServer().get().getPlayer(recipient.getName());
            if (player.isPresent()) {
                player.get().sendMessage(message.getCustomMessage());
            }
        }*/
        this.plugin.getGame().getServer().get().getOnlinePlayers().forEach(player -> player.sendMessage(message.getCustomMessage()));
    }

    @Subscribe
    public void onChat(PlayerChatEvent event) {
        Map<String, Object> data = new HashMap<>();
        /* TODO wait for recipient tracking
        Set<MinecraftPlayer> recipients = new HashSet<>();
        for (Player player : event.getRecipients()) {
            recipients.add(new MinecraftPlayer(player.getName(), player.getUniqueId()));
        } */
        // String format = event.getFormat(); TODO wait for format
        String message = event.getMessage().toLegacy();
        String sender = event.getPlayer().getName();
        data.put(Endpoint.MESSAGE_FORMAT, "<%1$s> %2$s"); // TODO wait for format
        data.put(Endpoint.MESSAGE_TEXT, message);
        // data.put(MinecraftEndpoint.PLAYER_LIST, recipients); TODO wait for recipient tracking
        data.put(Endpoint.SENDER_NAME, sender);
        this.plugin.getCraftIRC().getEndpointManager().sendMessage(new Message(this, String.format("<%1$s> %2$s", sender, message), data)); // TODO wait for format
    }
}