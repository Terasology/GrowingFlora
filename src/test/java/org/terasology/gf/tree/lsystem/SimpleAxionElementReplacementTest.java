// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gf.tree.lsystem;

import org.junit.Test;
import org.mockito.Mockito;
import org.terasology.engine.utilities.random.Random;

import static org.junit.Assert.assertEquals;

public class SimpleAxionElementReplacementTest {
    private final SimpleAxionElementReplacement replacements = new SimpleAxionElementReplacement("A");

    @Test
    public void testNoReplacements() {
        assertEquals("A", replacements.getReplacement(createRandom(0f), null, ""));
        assertEquals("A", replacements.getReplacement(createRandom(0.5f), null, ""));
        assertEquals("A", replacements.getReplacement(createRandom(0.99f), null, ""));
    }

    @Test
    public void testOneReplacement() {
        replacements.addReplacement(1, "B");
        assertEquals("B", replacements.getReplacement(createRandom(0f), null, ""));
        assertEquals("B", replacements.getReplacement(createRandom(0.5f), null, ""));
        assertEquals("B", replacements.getReplacement(createRandom(0.99f), null, ""));
    }

    @Test
    public void testTwoReplacementWholeProbability() {
        replacements.addReplacement(0.5f, "B");
        replacements.addReplacement(0.5f, "C");
        assertEquals("C", replacements.getReplacement(createRandom(0f), null, ""));
        assertEquals("B", replacements.getReplacement(createRandom(0.5f), null, ""));
        assertEquals("B", replacements.getReplacement(createRandom(0.99f), null, ""));
    }

    @Test
    public void testTwoReplacementWithDefault() {
        replacements.addReplacement(0.3f, "B");
        replacements.addReplacement(0.3f, "C");
        assertEquals("A", replacements.getReplacement(createRandom(0f), null, ""));
        assertEquals("A", replacements.getReplacement(createRandom(0.2f), null, ""));
        assertEquals("A", replacements.getReplacement(createRandom(0.3f), null, ""));
        assertEquals("C", replacements.getReplacement(createRandom(0.4f), null, ""));
        assertEquals("C", replacements.getReplacement(createRandom(0.5f), null, ""));
        assertEquals("C", replacements.getReplacement(createRandom(0.6f), null, ""));
        assertEquals("B", replacements.getReplacement(createRandom(0.7f), null, ""));
        assertEquals("B", replacements.getReplacement(createRandom(0.99f), null, ""));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGoingAboveOne() {
        replacements.addReplacement(0.3f, "B");
        replacements.addReplacement(0.8f, "C");
    }

    private Random createRandom(float value) {
        Random rnd = Mockito.mock(Random.class);
        Mockito.when(rnd.nextFloat()).thenReturn(value);
        return rnd;
    }
}
