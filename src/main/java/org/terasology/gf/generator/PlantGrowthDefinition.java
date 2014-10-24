/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.gf.generator;

import org.terasology.climateConditions.ClimateConditionsSystem;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generator.plugin.WorldGeneratorPlugin;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface PlantGrowthDefinition extends WorldGeneratorPlugin {
    String getPlantId();

    void generatePlant(long seed, CoreChunk chunk, int x, int y, int z, Region chunkRegion);

    /**
     * Returns how long to next update (if any). If null is returned, it's considered that the sapling was not initialized.
     *
     * @param worldProvider
     * @param blockEntityRegistry
     * @param plant
     * @return
     */
    Long initializeGeneratedPlant(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, EntityRef plant);

    /**
     * Returns how long to next update (if any). If null is returned, it's considered that the sapling was not initialized.
     *
     * @param worldProvider
     * @param blockEntityRegistry
     * @param plant
     * @return
     */
    Long initializePlantedPlant(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, EntityRef plant);

    /**
     * Returns how long to next update (if any). If null is returned, it's considered this plant requires no more updates.
     *
     *
     * @param worldProvider
     * @param environmentSystem
     *@param blockEntityRegistry
     * @param plant   @return
     */
    Long updatePlant(WorldProvider worldProvider, ClimateConditionsSystem environmentSystem, BlockEntityRegistry blockEntityRegistry, EntityRef plant);
}
