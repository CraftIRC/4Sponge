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
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.entity.player.PlayerChatEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
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
        List<MinecraftPlayer> players = this.plugin.getGame().getServer().getOnlinePlayers().stream().map(player -> new MinecraftPlayer(player.getName(), player.getUniqueId())).collect(Collectors.toCollection(LinkedList::new));
        message.getCustomData().put(MinecraftEndpoint.PLAYER_LIST, players);
    }

    @Override
    protected void receiveMessage(TargetedMessage message) {
        @SuppressWarnings("unchecked")
        List<MinecraftPlayer> recipients = (List<MinecraftPlayer>) message.getCustomData().get(MinecraftEndpoint.PLAYER_LIST);
        /* TODO wait for recipient tracking
        for (MinecraftPlayer recipient : recipients) {
            Optional<Player> player = this.plugin.getGame().getServer().getPlayer(recipient.getName());
            if (player.isPresent()) {
                player.get().sendMessage(Texts.of(message.getCustomMessage()));
            }
        }*/
        this.plugin.getGame().getServer().getOnlinePlayers().forEach(player -> player.sendMessage(Texts.of(message.getCustomMessage())));
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
        Text text = event.getMessage();
        if (text instanceof Text.Translatable) {
            Text.Translatable trans = (Text.Translatable) text;
            String message = Texts.toPlain((Text) trans.getArguments().get(1));
            String sender = Texts.toPlain((Text) trans.getArguments().get(0));
            String format = trans.getTranslation().get(Locale.ENGLISH);
            data.put(Endpoint.MESSAGE_FORMAT, format);
            data.put(Endpoint.MESSAGE_TEXT, message);
            // data.put(MinecraftEndpoint.PLAYER_LIST, recipients); TODO wait for recipient tracking
            data.put(Endpoint.SENDER_NAME, sender);
            this.plugin.getCraftIRC().getEndpointManager().sendMessage(new Message(this, String.format(format, sender, message), data));
        }
    }
}