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
package org.kitteh.craftirc.util;

import org.kitteh.irc.client.library.util.Sanity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Represents an ingame player
 */
public final class MinecraftPlayer {
    private final String name;
    private final UUID uniqueId;

    /**
     * Creates a MinecraftPlayer instance.
     *
     * @param name current name of the player
     * @param uniqueId the player's UUID
     */
    public MinecraftPlayer(@Nonnull String name, @Nonnull UUID uniqueId) {
        this.name = Sanity.nullCheck(name, "Name cannot be null");
        this.uniqueId = Sanity.nullCheck(uniqueId, "uniqueId cannot be null");
    }

    /**
     * Gets the name of this player.
     *
     * @return the player's name
     */
    @Nonnull
    public String getName() {
        return this.name;
    }

    /**
     * Gets the UUID of this player.
     *
     * @return the player's UUID
     */
    @Nonnull
    public UUID getUniqueID() {
        return this.uniqueId;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (o instanceof MinecraftPlayer) {
            MinecraftPlayer p = (MinecraftPlayer) o;
            return this.name.equals(p.name) && this.uniqueId.equals(p.uniqueId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (this.name.hashCode() * 7) + (this.uniqueId.hashCode() * 3);
    }
}