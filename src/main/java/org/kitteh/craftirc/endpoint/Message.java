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
package org.kitteh.craftirc.endpoint;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines a message.
 * <p/>
 * Messages are immutable, created by their originating {@link Endpoint}.
 */
public final class Message {
    private final Map<String, Object> data;
    private final String defaultMessage;
    private final Endpoint source;

    /**
     * Creates a new message.
     *
     * @param source originator of this message
     * @param defaultMessage this default message
     * @param data all associated data
     */
    public Message(@Nonnull Endpoint source, @Nonnull String defaultMessage, @Nonnull Map<String, Object> data) {
        this.source = source;
        this.defaultMessage = defaultMessage;
        this.data = Collections.unmodifiableMap(new HashMap<>(data));
    }

    /**
     * Gets the message's data.
     *
     * @return an immutable map representing the data
     */
    @Nonnull
    public Map<String, Object> getData() {
        return this.data;
    }

    /**
     * Gets the default message as created by the source {@link Endpoint}.
     *
     * @return the default message
     */
    @Nonnull
    public String getDefaultMessage() {
        return this.defaultMessage;
    }

    /**
     * Gets the source {@link Endpoint} of this message.
     *
     * @return the source Endpoint
     */
    @Nonnull
    public Endpoint getSource() {
        return this.source;
    }
}
