// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gf.tree.lsystem;

import org.terasology.engine.utilities.random.Random;

public class GrowthAxionElementReplacement implements AxionElementReplacement {
    private final String axion;
    private final float growth;

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
