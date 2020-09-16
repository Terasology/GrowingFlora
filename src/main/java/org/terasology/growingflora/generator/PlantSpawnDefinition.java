// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.growingflora.generator;

import org.terasology.engine.world.chunks.CoreChunk;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generator.plugin.WorldGeneratorPlugin;
import org.terasology.gestalt.naming.Name;
import org.terasology.growingflora.PlantType;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface PlantSpawnDefinition extends WorldGeneratorPlugin {
    PlantType getPlantType();

    Name getBiomeId();

    float getRarity();

    float getProbability();

    String getPlantId();

    void generatePlant(long seed, CoreChunk chunk, int x, int y, int z, Region chunkRegion);
}
