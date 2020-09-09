// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gf;

import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.Share;
import org.terasology.gf.generator.PlantGrowthDefinition;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
@Share(value = PlantRegistry.class)
public class PlantRegistryImpl extends BaseComponentSystem implements PlantRegistry {
    private final Map<String, PlantGrowthDefinition> plants = new HashMap<>();

    @Override
    public void addPlantType(String plantId, PlantGrowthDefinition plantGrowthDefinition) {
        plants.put(plantId, plantGrowthDefinition);
    }

    @Override
    public PlantGrowthDefinition getPlantGrowthDefinition(String plantId) {
        return plants.get(plantId);
    }
}
