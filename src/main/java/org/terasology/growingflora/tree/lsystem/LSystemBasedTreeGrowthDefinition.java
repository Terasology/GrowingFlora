// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.growingflora.tree.lsystem;

import org.terasology.climateConditions.ClimateConditionsSystem;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.chunks.CoreChunk;
import org.terasology.engine.world.generation.Region;
import org.terasology.growingflora.generator.ConnectedPlantGrowthDefinition;
import org.terasology.math.geom.Vector3i;

import java.util.Collection;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public abstract class LSystemBasedTreeGrowthDefinition implements ConnectedPlantGrowthDefinition {
    protected abstract AdvancedLSystemTreeDefinition getTreeDefinition();

    protected abstract String getGeneratedBlock();

    @Override
    public final void generatePlant(long seed, CoreChunk chunk, int x, int y, int z, Region chunkRegion) {
        getTreeDefinition().generateTree(seed, getGeneratedBlock(), chunk, x, y, z);
    }

    @Override
    public final Long initializeGeneratedPlant(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry,
                                               EntityRef plant) {
        return getTreeDefinition().setupTreeBaseBlock(worldProvider, blockEntityRegistry, plant);
    }

    @Override
    public Long initializePlantedPlant(WorldProvider worldProvider, ClimateConditionsSystem environmentSystem,
                                       BlockEntityRegistry blockEntityRegistry, EntityRef plant) {
        return getTreeDefinition().setupPlantedSapling(plant);
    }

    @Override
    public final Long requestedUpdatePlant(WorldProvider worldProvider, ClimateConditionsSystem environmentSystem,
                                           BlockEntityRegistry blockEntityRegistry, EntityRef plant) {
        return getTreeDefinition().updateTree(worldProvider, blockEntityRegistry, plant);
    }

    @Override
    public boolean randomUpdatePlant(WorldProvider worldProvider, ClimateConditionsSystem environmentSystem,
                                     BlockEntityRegistry blockEntityRegistry, EntityRef plant) {
        // Do nothing on random update
        return false;
    }

    @Override
    public boolean isBlockOwnedByPlant(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry,
                                       Vector3i block, EntityRef plant) {
        return getTreeDefinition().isBlockOwnedByPlant(worldProvider, blockEntityRegistry, block, plant);
    }

    @Override
    public Collection<Vector3i> getBlocksConnectedTo(WorldProvider worldProvider,
                                                     BlockEntityRegistry blockEntityRegistry, Vector3i block,
                                                     EntityRef plant) {
        return getTreeDefinition().getBlocksConnectedTo(worldProvider, blockEntityRegistry, block, plant);
    }
}
