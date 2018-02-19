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

import ninja.leaping.configurate.ConfigurationNode;
import org.kitteh.craftirc.CraftIRC;
import org.kitteh.craftirc.endpoint.link.Link;
import org.kitteh.craftirc.exceptions.CraftIRCInvalidConfigException;
import org.kitteh.craftirc.util.loadable.Loadable;

import javax.annotation.Nonnull;

/**
 * Endpoints are the origin and destination of messages tracked by CraftIRC.
 */
public abstract class Endpoint extends Loadable {
    /**
     * Constant defining the message data value "MESSAGE_FORMAT".
     */
    public static final String MESSAGE_FORMAT = "MESSAGE_FORMAT";
    /**
     * Constant defining the message data value "MESSAGE_TEXT".
     */
    public static final String MESSAGE_TEXT = "MESSAGE_TEXT";
    /**
     * Constant defining the message data value "SENDER_NAME".
     */
    public static final String SENDER_NAME = "SENDER_NAME";

    private String name;

    /**
     * Gets the name of this Endpoint.
     *
     * @return the name of this endpoint
     */
    @Nonnull
    public final String getName() {
        return this.name;
    }

    /**
     * Optional method to load any additional information for this Endpoint.
     * <p/>
     * Additional information is stored under 'extra' in the Endpoint's
     * definition.
     * <p/>
     * This method is not called if no such section exists.
     *
     * @param data the 'extra' section of the configuration
     */
    protected void loadExtra(@Nonnull ConfigurationNode data) throws CraftIRCInvalidConfigException {
        // By default, nothing extra to load
    }

    @Override
    protected final void load(@Nonnull CraftIRC plugin, @Nonnull ConfigurationNode data) throws CraftIRCInvalidConfigException {
        this.name = data.getNode("name").getString();
        final ConfigurationNode extra = data.getNode("extra");
        if (!extra.isVirtual()) {
            this.loadExtra(extra);
        }
    }

    /**
     * Processes a received message prior to processing by filters.
     *
     * @param message message to process
     */
    protected void preProcessReceivedMessage(@Nonnull TargetedMessage message) {
        // By default, don't do anything
    }

    /**
     * We get signal.
     * <p/>
     * A message received here has been processed by filters and is not
     * rejected by them.
     *
     * @param message the message to be displayed
     */
    protected abstract void receiveMessage(@Nonnull TargetedMessage message);

    /**
     * Receive a message and process.
     * <p/>
     * Sequence of events:
     * <ol>
     * <li>Pre-process</li>
     * <li>Run through filters, stop if rejected</li>
     * <li>Handle as received</li>
     * </ol>
     *
     * @param message the message sent by the source
     * @param link the link over which this message is sent
     */
    final void receiveMessage(@Nonnull Message message, @Nonnull Link link) {
        TargetedMessage targetedMessage = new TargetedMessage(this, message);
        try {
            this.preProcessReceivedMessage(targetedMessage);
        } catch (Throwable thrown) {
            CraftIRC.log().warning("Unable to preprocess a received message", thrown);
        }
        link.filterMessage(targetedMessage);
        if (targetedMessage.isRejected()) {
            return;
        }
        this.receiveMessage(targetedMessage);
    }
}