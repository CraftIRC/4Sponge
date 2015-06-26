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
import org.kitteh.craftirc.endpoint.Endpoint;
import org.kitteh.craftirc.endpoint.Message;
import org.kitteh.craftirc.endpoint.TargetedMessage;
import org.kitteh.craftirc.util.MinecraftPlayer;
import org.kitteh.craftirc.util.loadable.Loadable;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.entity.player.PlayerChatEvent;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.util.TextMessageException;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The standard {@link org.kitteh.craftirc.endpoint.Endpoint} for minecraft
 * chat messages.
 */
@Loadable.Type(name = "minecraft")
public class MinecraftEndpoint extends Endpoint {
    public static final String RECIPIENT_NAMES = "RECIPIENT_NAMES";

    private final SpongeIRC plugin;

    public MinecraftEndpoint(@Nonnull SpongeIRC plugin) {
        this.plugin = plugin;
        this.plugin.getGame().getEventManager().register(plugin, this);
    }

    @Override
    protected void preProcessReceivedMessage(@Nonnull TargetedMessage message) {
        Set<MinecraftPlayer> players = this.playerCollectionToMinecraftPlayer(this.plugin.getGame().getServer().getOnlinePlayers());
        message.getCustomData().put(MinecraftEndpoint.RECIPIENT_NAMES, players);
    }

    @Override
    protected void receiveMessage(@Nonnull TargetedMessage message) {
        @SuppressWarnings("unchecked")
        Set<MinecraftPlayer> recipients = (Set<MinecraftPlayer>) message.getCustomData().get(MinecraftEndpoint.RECIPIENT_NAMES);
        for (MinecraftPlayer recipient : recipients) {
            Optional<Player> player = this.plugin.getGame().getServer().getPlayer(recipient.getName());
            if (player.isPresent()) {
                try {
                    player.get().sendMessage(Texts.legacy().from(message.getCustomMessage()));
                } catch (TextMessageException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Subscribe
    public void onChat(@Nonnull PlayerChatEvent event) {
        Map<String, Object> data = new HashMap<>();
        Text text = event.getMessage();
        if (text instanceof Text.Translatable) {
            Text.Translatable trans = (Text.Translatable) text;
            List<Object> args = trans.getArguments();
            String message, sender;
            if (args.size() == 2 && (sender = this.getString(args.get(0))) != null && (message = this.getString(args.get(1))) != null) {
                String format = trans.getTranslation().get(Locale.ENGLISH);
                data.put(Endpoint.MESSAGE_FORMAT, format);
                data.put(Endpoint.MESSAGE_TEXT, message);
                Set<MinecraftPlayer> recipients = this.playerCollectionToMinecraftPlayer(this.plugin.getGame().getServer().getOnlinePlayers()); // TODO Collect recipients per event here.
                data.put(MinecraftEndpoint.RECIPIENT_NAMES, recipients);
                data.put(Endpoint.SENDER_NAME, sender);
                this.plugin.getCraftIRC().getEndpointManager().sendMessage(new Message(this, String.format(format, sender, message), data));
            }
        }
    }

    private String getString(Object o) {
        if (o instanceof String) {
            return (String) o;
        }
        if (o instanceof Text) {
            return Texts.toPlain((Text) o);
        }
        return null;
    }

    @Nonnull
    private Set<MinecraftPlayer> playerCollectionToMinecraftPlayer(@Nonnull Collection<Player> collection) {
        return collection.stream().map(player -> new MinecraftPlayer(player.getName(), player.getUniqueId())).collect(Collectors.toCollection(HashSet::new));
    }
}