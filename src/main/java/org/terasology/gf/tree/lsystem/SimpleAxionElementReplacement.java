// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gf.tree.lsystem;


import org.terasology.engine.utilities.random.Random;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class SimpleAxionElementReplacement implements AxionElementReplacement {
    private final String defaultReplacement;
    private final List<Float> probabilities = new ArrayList<>();
    private final List<AxionElementReplacement> replacements = new ArrayList<>();
    private float probabilitySum;

    public SimpleAxionElementReplacement(String defaultReplacement) {
        this.defaultReplacement = defaultReplacement;

        probabilities.add(1f);
        replacements.add(null);
    }

    public void addReplacement(float probability, String replacement) {
        addReplacement(probability, new StaticReplacementGenerator(replacement));
    }

    public void addReplacement(float probability, AxionElementReplacement replacement) {
        if (probabilitySum + probability > 1f) {
            throw new IllegalArgumentException("Sum of probabilities exceeds 1");
        }
        probabilitySum += probability;

        probabilities.add(1 - probabilitySum);
        replacements.add(replacement);
    }

    @Override
    public String getReplacement(Random random, String parameter, String currentAxiom) {
        for (int i = 0, size = probabilities.size(); i < size - 1; i++) {
            float randomValue = random.nextFloat();
            if (probabilities.get(i) > randomValue && probabilities.get(i + 1) <= randomValue) {
                return replacements.get(i + 1).getReplacement(random, parameter, currentAxiom);
            }
        }
        return defaultReplacement;
    }

    private final class StaticReplacementGenerator implements AxionElementReplacement {
        private final String result;

        private StaticReplacementGenerator(String result) {
            this.result = result;
        }

        @Override
        public String getReplacement(Random random, String parameter, String currentAxion) {
            return result;
        }
    }
}
