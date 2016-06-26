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

import com.google.common.eventbus.Subscribe;
import org.kitteh.craftirc.endpoint.Endpoint;
import org.kitteh.craftirc.endpoint.Message;
import org.kitteh.craftirc.endpoint.TargetedMessage;
import org.kitteh.craftirc.util.MinecraftPlayer;
import org.kitteh.craftirc.util.loadable.Loadable;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The standard {@link Endpoint} for minecraft /me messages.
 */
@Loadable.Type(name = "mc-me")
public class MeEndpoint extends MinecraftEndpoint {
    public MeEndpoint(@Nonnull SpongeIRC plugin) {
        super(plugin);
    }

    @Override
    protected void receiveMessage(@Nonnull TargetedMessage targetedMessage) {
        // NO-OP
    }

    @Subscribe
    public void onChat(@Nonnull CommandEvent event) {
        CommandSource source = event.getSource();
        if (!(source instanceof Player)) {
            return;
        }
        Player sender = (Player) source;
        Map<String, Object> data = new HashMap<>();
        Text text = event.getMessage();
        if (text instanceof Text.Translatable) {
            Text.Translatable trans = (Text.Translatable) text;
            List<Object> args = trans.getArguments();
            String message, sender;
            if (args.size() == 2 && (sender = this.getStringFromStringOrText(args.get(0))) != null && (message = this.getStringFromStringOrText(args.get(1))) != null) {
                String format = trans.getTranslation().get(Locale.ENGLISH);
                data.put(Endpoint.MESSAGE_FORMAT, format);
                data.put(Endpoint.MESSAGE_TEXT, message);
                Set<MinecraftPlayer> recipients = this.playerCollectionToMinecraftPlayer(this.getPlugin().getGame().getServer().getOnlinePlayers()); // TODO Collect recipients per event here.
                data.put(MeEndpoint.RECIPIENT_NAMES, recipients);
                data.put(Endpoint.SENDER_NAME, sender);
                this.getPlugin().getCraftIRC().getEndpointManager().sendMessage(new Message(this, String.format(format, sender, message), data));
            }
        }
    }
}