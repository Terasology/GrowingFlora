// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.gf;

import org.joml.Vector3ic;
import org.terasology.climateConditions.ClimateConditionsSystem;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;
import org.terasology.gf.util.EnvironmentLocalParameters;
import org.terasology.gf.util.LocalParameters;

@RegisterSystem
@Share(EnvironmentParametersSystem.class)
public class EnvironmentParametersSystem extends BaseComponentSystem {
    @In
    private ClimateConditionsSystem climateConditionsSystem;

    public LocalParameters createLocalParameters(Vector3ic position) {
        return new EnvironmentLocalParameters(climateConditionsSystem, position);
    }
}
