// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.gf.util;

import org.joml.Vector3ic;
import org.terasology.climateConditions.ClimateConditionsSystem;

public class EnvironmentLocalParameters implements LocalParameters {
    private ClimateConditionsSystem environmentSystem;
    private Vector3ic location;

    public EnvironmentLocalParameters(ClimateConditionsSystem environmentSystem, Vector3ic location) {
        this.environmentSystem = environmentSystem;
        this.location = location;
    }

    @Override
    public float getTemperature() {
        return environmentSystem.getTemperature(location.x(), location.y(), location.z());
    }

    @Override
    public float getHumidity() {
        return environmentSystem.getHumidity(location.x(), location.y(), location.z());
    }
}
