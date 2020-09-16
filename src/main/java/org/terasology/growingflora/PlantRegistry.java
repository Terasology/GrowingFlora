// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.growingflora;

import org.terasology.growingflora.generator.PlantGrowthDefinition;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface PlantRegistry {
    void addPlantType(String plantId, PlantGrowthDefinition plantDefinition);

    PlantGrowthDefinition getPlantGrowthDefinition(String plantId);
}
