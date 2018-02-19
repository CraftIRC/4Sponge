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

import org.kitteh.craftirc.CraftIRC;
import org.kitteh.craftirc.endpoint.link.Link;
import org.kitteh.craftirc.util.shutdownable.WackyWavingInterruptableArmFlailingThreadMan;
import org.kitteh.irc.client.library.util.Pair;

import javax.annotation.Nonnull;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Distributes messages.
 * <p/>
 * YOU GET A MESSAGE, AND YOU GET A MESSAGE! EVERYBODY GETS A MESSAGE!
 */
final class MessageDistributor extends Thread {
    private final EndpointManager endpointManager;
    private final ConcurrentLinkedQueue<Message> messages = new ConcurrentLinkedQueue<>();

    MessageDistributor(@Nonnull EndpointManager manager, @Nonnull CraftIRC plugin) {
        this.endpointManager = manager;
        plugin.trackShutdownable(new WackyWavingInterruptableArmFlailingThreadMan(this));
        this.start();
    }

    void addMessage(@Nonnull Message message) {
        this.messages.add(message);
        synchronized (this.messages) {
            this.messages.notify();
        }
    }

    @Override
    public void run() {
        long timeTrack;
        while (!this.isInterrupted()) {
            timeTrack = System.currentTimeMillis();
            Message message = this.messages.poll();
            if (message != null) {
                for (Pair<Link, Endpoint> pair : this.endpointManager.getDestinations(message.getSource().getName())) {
                    pair.getRight().receiveMessage(message, pair.getLeft());
                }
            }
            if (this.messages.isEmpty()) {
                synchronized (this.messages) {
                    try {
                        this.messages.wait();
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
            timeTrack = System.currentTimeMillis() - timeTrack;
            if (timeTrack < 50 && timeTrack > 0) {
                try {
                    Thread.sleep(50 - timeTrack);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }
    }
}