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
package org.kitteh.craftirc.endpoint.filter.defaults;

import org.kitteh.craftirc.endpoint.TargetedMessage;
import org.kitteh.craftirc.endpoint.filter.Filter;
import org.kitteh.craftirc.util.loadable.Load;
import org.kitteh.craftirc.util.loadable.Loadable;

import javax.annotation.Nonnull;

/**
 * Anti highlight aww yes.
 */
@Loadable.Type(name = "antihighlight")
public class AntiHighlight extends Filter {
    @Load
    private String splitter;
    @Load
    private String variable;

    @Override
    public void processMessage(@Nonnull TargetedMessage message) {
        if (message.getCustomData().containsKey(this.variable)) {
            String oldValue = message.getCustomData().get(this.variable).toString();
            if (oldValue.length() > 1) {
                StringBuilder builder = new StringBuilder();
                for (char c : oldValue.toCharArray()) {
                    builder.append(c).append(this.splitter);
                }
                builder.setLength(builder.length() - this.splitter.length());
                String newValue = builder.toString();
                message.getCustomData().put(this.variable, newValue);
                message.setCustomMessage(message.getCustomMessage().replace(oldValue, newValue));
            }
        }
    }
}