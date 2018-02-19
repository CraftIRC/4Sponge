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

import org.kitteh.craftirc.endpoint.Endpoint;
import org.kitteh.craftirc.endpoint.TargetedMessage;
import org.kitteh.craftirc.util.MinecraftPlayer;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageReceiver;
import org.spongepowered.api.text.serializer.TextSerializers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Abstract root of Minecraft {$link}endpoints.
 */
public abstract class MinecraftEndpoint extends Endpoint {
    public static final String RECIPIENT_NAMES = "RECIPIENT_NAMES";

    private final SpongeIRC plugin;

    public MinecraftEndpoint(@Nonnull SpongeIRC plugin) {
        this.plugin = plugin;
        this.plugin.registerEndpoint(this);
    }

    @Nonnull
    protected SpongeIRC getPlugin() {
        return this.plugin;
    }

    @Override
    protected void preProcessReceivedMessage(@Nonnull TargetedMessage message) {
        Set<MinecraftPlayer> players = this.collectionToMinecraftPlayer(this.plugin.getGame().getServer().getOnlinePlayers());
        message.getCustomData().put(MinecraftEndpoint.RECIPIENT_NAMES, players);
    }

    @Nullable
    protected String getStringFromStringOrText(Object o) {
        if (o instanceof String) {
            return (String) o;
        }
        if (o instanceof Text) {
            return TextSerializers.PLAIN.serialize((Text) o);
        }
        return null;
    }

    @Nonnull
    protected Set<MinecraftPlayer> collectionToMinecraftPlayer(@Nonnull Collection<? extends MessageReceiver> collection) {
        return collection.stream().filter(source -> source instanceof Player).map(player -> new MinecraftPlayer(((Player) player).getName(), ((Player) player).getUniqueId())).collect(Collectors.toCollection(HashSet::new));
    }

    @Nonnull
    protected Set<MinecraftPlayer> commandSourceIterableToMinecraftPlayer(@Nonnull Iterable<? extends CommandSource> iterable) {
        Set<MinecraftPlayer> set = new HashSet<>();
        iterable.forEach(source -> {
            if (source instanceof Player) {
                set.add(new MinecraftPlayer(source.getName(), ((Player) source).getUniqueId()));
            }
        });
        return set;
    }
}
