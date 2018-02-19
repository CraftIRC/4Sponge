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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Filter of information, via regular expression.
 */
@Loadable.Type(name = "regex")
public class RegexFilter extends Filter {
    public enum Action {
        ALLOW,
        DROP,
        REPLACE,
        STORE;

        private static final Map<String, Action> nameMap = new HashMap<>();
        private static final String names;

        static {
            StringBuilder builder = new StringBuilder();
            for (Action action : Action.values()) {
                nameMap.put(action.name(), action);
                builder.append(action.name()).append(", ");
            }
            if (builder.length() > 0) {
                builder.setLength(builder.length() - ", ".length());
            }
            names = builder.toString();
        }

        private static Action getByName(String name) {
            if (name == null) {
                return null;
            }
            return Action.nameMap.get(name.toUpperCase());
        }
    }

    public enum Match {
        FULL,
        PARTIAL;

        private static final Map<String, Match> nameMap = new HashMap<>();

        static {
            for (Match match : Match.values()) {
                nameMap.put(match.name(), match);
            }
        }

        private static Match getByName(String name) {
            if (name == null) {
                return null;
            }
            return Match.nameMap.get(name.toUpperCase());
        }
    }

    private static final Pattern NAMED_GROUP = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>");

    private Action action;
    private Match match;
    private Pattern pattern;
    private String replacement;
    @Load
    private String value;
    private final List<String> namedGroups = new LinkedList<>();

    @Override
    protected void load(@Nonnull ConfigurationNode data) throws CraftIRCInvalidConfigException {
        final String pattern;
        if ((pattern = data.getNode("pattern").getString()) == null) {
            throw new CraftIRCInvalidConfigException("Regex pattern requires a 'pattern' defined");
        }
        this.pattern = Pattern.compile(pattern);
        if ((this.action = Action.getByName(data.getNode("action").getString())) == null) {
            throw new CraftIRCInvalidConfigException("Regex pattern requires an 'action' defined. Valid action types: " + Action.names);
        }
        this.match = Match.getByName(data.getNode("match").getString());
        if (this.match == null) {
            this.match = Match.PARTIAL;
        }
        switch (this.action) {
            case REPLACE:
                if ((this.replacement = data.getNode("replacement").getString()) == null) {
                    throw new CraftIRCInvalidConfigException("Regex pattern replace action requires 'replacement' to be set");
                }
                break;
            case STORE:
                Matcher namedGroupMatcher = NAMED_GROUP.matcher(pattern);
                while (namedGroupMatcher.find()) {
                    this.namedGroups.add(namedGroupMatcher.group(1));
                }
                if (this.namedGroups.isEmpty()) {
                    throw new CraftIRCInvalidConfigException("To use the STORE action, a named matching group must be defined");
                }
                // TODO: Provide for defining which, in multiple match cases, found item is stored
                break;
            default:
                // Nothing else to do
        }
    }

    @Override
    public void processMessage(@Nonnull TargetedMessage message) {
        String val = message.getCustomData().get(this.value).toString();
        Matcher matcher = this.pattern.matcher(val);
        boolean matches;
        switch (this.match) {
            case FULL:
                matches = matcher.matches();
                break;
            case PARTIAL:
            default:
                matches = matcher.find();
        }
        switch (this.action) {
            case ALLOW:
                if (!matches) {
                    message.reject();
                }
                break;
            case DROP:
                if (matches) {
                    message.reject();
                }
                break;
            case REPLACE:
                if (matches) {
                    message.getCustomData().put(this.value, matcher.replaceAll(this.replacement));
                }
                break;
            case STORE:
                do {
                    for (String name : this.namedGroups) {
                        String match = matcher.group(name);
                        if (match != null) {
                            message.getCustomData().put(name, match);
                        }
                    }
                } while (this.match == Match.PARTIAL && matcher.find());
                break;
            default:
                // panic
        }
    }
}