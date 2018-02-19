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

import org.kitteh.craftirc.endpoint.Endpoint;
import org.kitteh.craftirc.endpoint.TargetedMessage;
import org.kitteh.craftirc.endpoint.defaults.IRCEndpoint;
import org.kitteh.craftirc.endpoint.filter.Filter;
import org.kitteh.craftirc.util.WrappedMap;
import org.kitteh.craftirc.util.loadable.Loadable;
import org.kitteh.irc.client.library.util.Format;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Color conversion.
 */
@Loadable.Type(name = "color")
public class Colors extends Filter {
    private enum Matches {
        BLACK(Format.BLACK, '0'),
        DARK_BLUE(Format.DARK_BLUE, '1'),
        DARK_GREEN(Format.DARK_GREEN, '2'),
        DARK_AQUA(Format.TEAL, '3'),
        DARK_RED(Format.BROWN, '4'),
        DARK_PURPLE(Format.PURPLE, '5'),
        GOLD(Format.OLIVE, '6'),
        GRAY(Format.LIGHT_GRAY, '7'),
        DARK_GRAY(Format.DARK_GRAY, '8'),
        BLUE(Format.BLUE, '9'),
        GREEN(Format.GREEN, 'A'),
        AQUA(Format.CYAN, 'B'),
        RED(Format.RED, 'C'),
        LIGHT_PURPLE(Format.MAGENTA, 'D'),
        YELLOW(Format.YELLOW, 'E'),
        WHITE(Format.WHITE, 'F');

        private Format irc;
        private char mc;

        Matches(Format irc, char mc) {
            this.irc = irc;
            this.mc = mc;
        }

        private static final Map<Integer, String> IRC_MAP;
        private static final Pattern IRC_PATTERN;
        private static final Map<Character, String> MC_MAP;
        private static final Pattern MC_PATTERN;

        static {
            IRC_MAP = new HashMap<>();
            MC_MAP = new HashMap<>();
            for (Matches matches : values()) {
                IRC_MAP.put(matches.irc.getColorChar(), "\u00A7" + matches.mc);
                MC_MAP.put(matches.mc, matches.irc.toString());
            }
            IRC_PATTERN = Pattern.compile(Format.COLOR_CHAR + "([0-9]{1,2})(?:,[0-9]{1,2})?");
            MC_PATTERN = Pattern.compile("\u00A7([a-z0-9])", Pattern.CASE_INSENSITIVE);
        }

        static String getIRCByMC(char mc) {
            return MC_MAP.get(mc);
        }

        static String getMCByIRC(int irc) {
            return IRC_MAP.get(irc);
        }
    }

    @Override
    public void processMessage(@Nonnull TargetedMessage message) {
        Endpoint origin = message.getTarget();
        message.setCustomMessage(this.process(origin, message.getCustomMessage()));
        WrappedMap<String, Object> map = message.getCustomData();
        this.process(origin, map, Endpoint.SENDER_NAME);
        this.process(origin, map, Endpoint.MESSAGE_TEXT);
    }

    private void process(Endpoint origin, WrappedMap<String, Object> map, String key) {
        Object o = map.get(key);
        if (o instanceof String) {
            map.put(key, this.process(origin, (String) map.get(key)));
        }
    }

    private String process(Endpoint origin, String original) {
        return origin instanceof IRCEndpoint ? toIRC(original) : toMC(original);
    }

    private String toIRC(String input) {
        Matcher matcher = Matches.MC_PATTERN.matcher(input);
        int currentIndex = 0;
        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            int next = matcher.start();
            if (currentIndex < next) {
                builder.append(input.substring(currentIndex, next));
            }
            currentIndex = matcher.end();
            char s = matcher.group(1).toUpperCase().charAt(0);
            if (s <= 'F') {
                builder.append(Matches.getIRCByMC(s));
            } else if (s == 'R') {
                builder.append(Format.RESET);
            }
        }
        if (currentIndex < input.length()) {
            builder.append(input.substring(currentIndex));
        }
        return builder.append(Format.RESET).toString();
    }

    private String toMC(String input) {
        Matcher matcher = Matches.IRC_PATTERN.matcher(input);
        input = input.replace(Format.BOLD.toString(), "");
        input = input.replace(Format.UNDERLINE.toString(), "");
        input = input.replace(Format.REVERSE.toString(), "");
        input = input.replace(Format.RESET.toString(), "\u00A7r");
        int currentIndex = 0;
        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            int next = matcher.start();
            if (currentIndex < next) {
                builder.append(input.substring(currentIndex, next));
            }
            currentIndex = matcher.end();
            int i;
            try {
                i = Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException ignored) {
                continue;
            }
            builder.append(Matches.getMCByIRC(i));
        }
        if (currentIndex < input.length()) {
            builder.append(input.substring(currentIndex));
        }
        return builder.append("\u00A7r").toString();
    }
}