/*
 * * Copyright (C) 2015-2017 Matt Baxter http://kitteh.org
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
package org.kitteh.craftirc.sponge.util;

import org.kitteh.irc.client.library.util.Sanity;
import org.slf4j.Logger;

import javax.annotation.Nonnull;

/**
 * Implements CraftIRC's logger wrapper.
 */
public class Log4JWrapper implements org.kitteh.craftirc.util.Logger {
    private final Logger logger;

    public Log4JWrapper(Logger logger) {
        Sanity.nullCheck(logger, "Logger cannot be null");
        this.logger = logger;
    }

    @Override
    public void info(@Nonnull String info) {
        this.logger.info(info);
    }

    @Override
    public void warning(@Nonnull String warn) {
        this.logger.warn(warn);
    }

    @Override
    public void warning(@Nonnull String warn, @Nonnull Throwable thrown) {
        this.logger.warn(warn, thrown);
    }

    @Override
    public void severe(@Nonnull String severe) {
        this.logger.error(severe);
    }

    @Override
    public void severe(@Nonnull String severe, @Nonnull Throwable thrown) {
        this.logger.error(severe, thrown);
    }
}