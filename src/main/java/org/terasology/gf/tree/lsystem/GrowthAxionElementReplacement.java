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

public class GrowthAxionElementReplacement implements AxionElementReplacement {
    private String axion;
    private float growth;

    public GrowthAxionElementReplacement(String axion, float growth) {
        this.axion = axion;
        this.growth = growth;
    }

    @Override
    public String getReplacement(Random random, String parameter, String currentAxion) {
        float length = Float.parseFloat(parameter);
        return axion + "(" + (length * growth) + ")";
    }
}
