package org.kitteh.craftirc.sponge;

import org.kitteh.craftirc.endpoint.Endpoint;
import org.kitteh.craftirc.endpoint.TargetedMessage;
import org.kitteh.craftirc.util.MinecraftPlayer;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.Texts;

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
        this.plugin.getGame().getEventManager().register(plugin, this);
    }

    @Nonnull
    protected SpongeIRC getPlugin() {
        return this.plugin;
    }

    @Override
    protected void preProcessReceivedMessage(@Nonnull TargetedMessage message) {
        Set<MinecraftPlayer> players = this.playerCollectionToMinecraftPlayer(this.plugin.getGame().getServer().getOnlinePlayers());
        message.getCustomData().put(MeEndpoint.RECIPIENT_NAMES, players);
    }

    @Nullable
    protected String getStringFromStringOrText(Object o) {
        if (o instanceof String) {
            return (String) o;
        }
        if (o instanceof Text) {
            return Texts.toPlain((Text) o);
        }
        return null;
    }

    @Nonnull
    protected Set<MinecraftPlayer> playerCollectionToMinecraftPlayer(@Nonnull Collection<Player> collection) {
        return collection.stream().map(player -> new MinecraftPlayer(player.getName(), player.getUniqueId())).collect(Collectors.toCollection(HashSet::new));
    }
}