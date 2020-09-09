// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gf.generator;

import org.terasology.climateConditions.ClimateConditionsSystem;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.chunks.CoreChunk;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generator.plugin.WorldGeneratorPlugin;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface PlantGrowthDefinition extends WorldGeneratorPlugin {
    String getPlantId();

    void generatePlant(long seed, CoreChunk chunk, int x, int y, int z, Region chunkRegion);

    /**
     * Returns how long to next update (if any). If null is returned, it's considered that the sapling was not
     * initialized.
     *
     * @param worldProvider
     * @param blockEntityRegistry
     * @param plant
     * @return
     */
    Long initializeGeneratedPlant(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry,
                                  EntityRef plant);

    /**
     * Returns how long to next update (if any). If null is returned, it's considered that the sapling was not
     * initialized.
     *
     * @param worldProvider
     * @param environmentSystem
     * @param blockEntityRegistry
     * @param plant @return
     */
    Long initializePlantedPlant(WorldProvider worldProvider, ClimateConditionsSystem environmentSystem,
                                BlockEntityRegistry blockEntityRegistry, EntityRef plant);

    /**
     * Returns how long to next update (if any). If null is returned, it's considered this plant requires no more
     * updates.
     *
     * @param worldProvider
     * @param environmentSystem
     * @param blockEntityRegistry
     * @param plant @return
     * @return
     */
    Long requestedUpdatePlant(WorldProvider worldProvider, ClimateConditionsSystem environmentSystem,
                              BlockEntityRegistry blockEntityRegistry, EntityRef plant);

    /**
     * Called randomly on a plant. If true is returned - this plant will no longer receive requested updates.
     *
     * @param worldProvider
     * @param environmentSystem
     * @param blockEntityRegistry
     * @param plant
     * @return
     */
    boolean randomUpdatePlant(WorldProvider worldProvider, ClimateConditionsSystem environmentSystem,
                              BlockEntityRegistry blockEntityRegistry, EntityRef plant);
}
