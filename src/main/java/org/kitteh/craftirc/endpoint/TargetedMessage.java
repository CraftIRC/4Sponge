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

import org.kitteh.craftirc.util.WrappedMap;

import javax.annotation.Nonnull;

/**
 * Wraps a message as received by a particular {@link Endpoint}.
 */
public final class TargetedMessage {
    private final Message originatingMessage;
    private final Endpoint target;
    private String customMessage;
    private final WrappedMap<String, Object> customData;
    private boolean rejected = false;

    /**
     * Creates a message targetted at an {@link Endpoint}.
     *
     * @param target message destination
     * @param originatingMessage the message being sent
     */
    public TargetedMessage(@Nonnull Endpoint target, @Nonnull Message originatingMessage) {
        this.target = target;
        this.originatingMessage = originatingMessage;
        this.customData = new WrappedMap<>(originatingMessage.getData());
        this.customMessage = originatingMessage.getDefaultMessage();
    }

    /**
     * Gets any custom data associated with this message. The data can be
     * modified specifically for this TargetedMessage.
     *
     * @return the custom data associated with the message
     */
    @Nonnull
    public WrappedMap<String, Object> getCustomData() {
        return this.customData;
    }

    /**
     * Gets the current message to be outputted to the target Endpoint. By
     * default, this message is {@link Message#getDefaultMessage()}.
     *
     * @return the message to be displayed to the Endpoint
     */
    @Nonnull
    public String getCustomMessage() {
        return this.customMessage;
    }

    /**
     * Sets the message to be output to the target Endpoint.
     *
     * @param message the new message
     * @return the previously set message
     */
    @Nonnull
    public String setCustomMessage(@Nonnull String message) {
        String oldMessage = this.customMessage;
        this.customMessage = message;
        return oldMessage;
    }

    /**
     * Gets the target of this message.
     *
     * @return the Endpoint at which this message is targetted
     */
    @Nonnull
    public Endpoint getTarget() {
        return this.target;
    }

    /**
     * Gets the message sent by the source.
     *
     * @return the originating message
     */
    @Nonnull
    public Message getOriginatingMessage() {
        return this.originatingMessage;
    }

    /**
     * Sets a message as being rejected by its destination.
     */
    public void reject() {
        this.rejected = true;
    }

    /**
     * Gets if the message is rejected.
     *
     * @return true if rejected
     */
    public boolean isRejected() {
        return this.rejected;
    }
}