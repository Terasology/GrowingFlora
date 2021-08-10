/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.gf.tree.lsystem;


import org.terasology.engine.utilities.random.Random;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class SimpleAxionElementReplacement implements AxionElementReplacement {
    private float probabilitySum;
    private String defaultReplacement;
    private List<Float> probabilities = new ArrayList<>();
    private List<AxionElementReplacement> replacements = new ArrayList<>();

    public SimpleAxionElementReplacement(String defaultReplacement) {
        this.defaultReplacement = defaultReplacement;

        probabilities.add(1f);
        replacements.add(null);
    }

    /**
     * @return {@code this}, to allow for method chaining
     */
    public SimpleAxionElementReplacement addReplacement(float probability, String replacement) {
        return addReplacement(probability, new StaticReplacementGenerator(replacement));
    }

    /**
     * @return {@code this}, to allow for method chaining
     */
    public SimpleAxionElementReplacement addReplacement(float probability, AxionElementReplacement replacement) {
        if (probabilitySum + probability > 1f) {
            throw new IllegalArgumentException("Sum of probabilities exceeds 1");
        }
        probabilitySum += probability;

        probabilities.add(1 - probabilitySum);
        replacements.add(replacement);

        return this;
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
        private String result;

        private StaticReplacementGenerator(String result) {
            this.result = result;
        }

        @Override
        public String getReplacement(Random random, String parameter, String currentAxion) {
            return result;
        }
    }
}
