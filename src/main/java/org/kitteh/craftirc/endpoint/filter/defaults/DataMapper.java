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

import ninja.leaping.configurate.ConfigurationNode;
import org.kitteh.craftirc.endpoint.TargetedMessage;
import org.kitteh.craftirc.endpoint.filter.Filter;
import org.kitteh.craftirc.exceptions.CraftIRCInvalidConfigException;
import org.kitteh.craftirc.util.loadable.Load;
import org.kitteh.craftirc.util.loadable.Loadable;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Maps data to a message.
 */
@Loadable.Type(name = "datamapper")
public class DataMapper extends Filter {
    private static final Pattern PERCENT_VARIABLE = Pattern.compile("%([^ %\\n]+)%");
    private String format;
    @Load
    private String message;
    private List<String> variables;

    @Nonnull
    public String getMessageFormat() {
        return this.message;
    }

    @Override
    public void processMessage(@Nonnull TargetedMessage message) {
        Object[] vars = new Object[this.variables.size()];
        for (int i = 0; i < vars.length; i++) {
            Object data = message.getCustomData().get(this.variables.get(i));
            vars[i] = data == null ? "" : data.toString();
        }
        message.setCustomMessage(String.format(this.format, vars));
    }

    @Override
    protected void load(@Nonnull ConfigurationNode data) throws CraftIRCInvalidConfigException {
        Matcher matcher = PERCENT_VARIABLE.matcher(this.message);
        this.variables = new LinkedList<>();
        StringBuilder builder = new StringBuilder();
        int last = 0;
        while (matcher.find()) {
            builder.append(this.message.substring(last, matcher.start())).append("%s");
            this.variables.add(matcher.group(1));
            last = matcher.end();
        }
        builder.append(message.substring(last, message.length()));
        this.format = builder.toString();
    }
}
