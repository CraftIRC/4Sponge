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
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.message.MessageChannelEvent;
import org.spongepowered.api.text.LiteralText;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TranslatableText;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * The standard {@link Endpoint} for minecraft chat messages.
 */
@Loadable.Type(name = "mc-chat")
public class ChatEndpoint extends MinecraftEndpoint {
    public ChatEndpoint(@Nonnull SpongeIRC plugin) {
        super(plugin);
    }

    @Override
    protected void receiveMessage(@Nonnull TargetedMessage message) {
        @SuppressWarnings("unchecked")
        Set<MinecraftPlayer> recipients = (Set<MinecraftPlayer>) message.getCustomData().get(ChatEndpoint.RECIPIENT_NAMES);
        for (MinecraftPlayer recipient : recipients) {
            Optional<Player> player = this.getPlugin().getGame().getServer().getPlayer(recipient.getName());
            if (player.isPresent()) {
                player.get().sendMessage(TextSerializers.LEGACY_FORMATTING_CODE.deserialize(message.getCustomMessage()));
            }
        }
    }

    @Listener
    public void onChat(@Nonnull MessageChannelEvent.Chat event) {
        if (!event.getChannel().isPresent() || !event.getCause().first(Player.class).isPresent()) {
            return; // Not a player chatting
        }
        Map<String, Object> data = new HashMap<>();
        Text text = event.getOriginalMessage();
        Set<MinecraftPlayer> recipients = this.collectionToMinecraftPlayer(event.getChannel().get().getMembers());
        data.put(ChatEndpoint.RECIPIENT_NAMES, recipients);
        //text.withChildren().forEach(ch -> org.kitteh.craftirc.CraftIRC.log().info(ch.toString()));
        if (text instanceof TranslatableText) {
            TranslatableText trans = (TranslatableText) text;
            List<Object> args = trans.getArguments();
            String message, sender;
            if (args.size() == 2 && (sender = this.getStringFromStringOrText(args.get(0))) != null && (message = this.getStringFromStringOrText(args.get(1))) != null) {
                String format = trans.getTranslation().get(Locale.ENGLISH);
                data.put(Endpoint.MESSAGE_FORMAT, format);
                data.put(Endpoint.MESSAGE_TEXT, message);
                data.put(Endpoint.SENDER_NAME, sender);
                this.getPlugin().getCraftIRC().ifPresent(craftIRC -> craftIRC.getEndpointManager().sendMessage(new Message(this, String.format(format, sender, message), data)));
            }
        } else if (text instanceof LiteralText) {
            LiteralText literalText = (LiteralText) text;
            //literalText.withChildren().forEach(ch -> CraftIRC.log().info(ch.toString()));
            List<Text> texts = new ArrayList<>();
            literalText.withChildren().forEach(t -> {
                if (t.getChildren().isEmpty()) {
                    texts.add(t);
                }
            });
            if (texts.size() == 3) {
                Text one = texts.get(0);
                Text three = texts.get(2);
                if (one instanceof LiteralText && three instanceof LiteralText) {
                    String message = ((LiteralText) three).getContent();
                    data.put(Endpoint.MESSAGE_TEXT, message);
                    String sender = ((LiteralText) one).getContent();
                    data.put(Endpoint.SENDER_NAME, sender);
                    this.getPlugin().getCraftIRC().ifPresent(craftIRC -> craftIRC.getEndpointManager().sendMessage(new Message(this, String.format("<%s> %s", sender, message), data)));
                }
            }
        }
    }
}