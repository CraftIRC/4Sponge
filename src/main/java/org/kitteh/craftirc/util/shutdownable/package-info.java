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
/**
 * When running systems that need some sort of cleanup on plugin shutdown, be
 * it interrupting a thread or storing/sending one final bit of information,
 * the {@link org.kitteh.craftirc.util.shutdownable.Shutdownable} interface
 * is there to handle your shutting down needs. The main plugin class
 * provides the {@link
 * org.kitteh.craftirc.CraftIRC#trackShutdownable(Shutdownable)} method to
 * automagically handle shutting down of a given Shutdownable. For a premade
 * Thread handler, see {@link
 * org.kitteh.craftirc.util.shutdownable.WackyWavingInterruptableArmFlailingThreadMan}
 */
package org.kitteh.craftirc.util.shutdownable;
