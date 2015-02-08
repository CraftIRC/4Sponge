package org.kitteh.craftirc.sponge;

import org.junit.Assert;
import org.junit.Test;
import org.spongepowered.api.plugin.Plugin;

/**
 * Confirms the version has been set.
 */
public class AnnotatedVersionTest {
    @Test
    public void test() {
        Plugin annotation = SpongeIRC.class.getAnnotation(Plugin.class);
        Assert.assertTrue("Failed to set version! Found: " + annotation.version(), !annotation.version().equals(SpongeIRC.MAGIC_VERSION));
    }
}