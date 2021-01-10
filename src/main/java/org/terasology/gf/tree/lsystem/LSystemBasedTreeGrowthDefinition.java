// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gf.tree.lsystem;

import org.joml.Vector3i;
import org.terasology.climateConditions.ClimateConditionsSystem;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.gf.generator.ConnectedPlantGrowthDefinition;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;

import java.util.Collection;

public abstract class LSystemBasedTreeGrowthDefinition implements ConnectedPlantGrowthDefinition {
    protected abstract AdvancedLSystemTreeDefinition getTreeDefinition();

    protected abstract String getGeneratedBlock();

    @Override
    public final void generatePlant(long seed, CoreChunk chunk, int x, int y, int z, Region chunkRegion) {
        getTreeDefinition().generateTree(seed, getGeneratedBlock(), chunk, x, y, z);
    }

    @Override
    public final Long initializeGeneratedPlant(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, EntityRef plant) {
        return getTreeDefinition().setupTreeBaseBlock(worldProvider, blockEntityRegistry, plant);
    }

    @Override
    public Long initializePlantedPlant(WorldProvider worldProvider, ClimateConditionsSystem environmentSystem, BlockEntityRegistry blockEntityRegistry, EntityRef plant) {
        return getTreeDefinition().setupPlantedSapling(plant);
    }

    @Override
    public final Long requestedUpdatePlant(WorldProvider worldProvider, ClimateConditionsSystem environmentSystem, BlockEntityRegistry blockEntityRegistry, EntityRef plant) {
        return getTreeDefinition().updateTree(worldProvider, blockEntityRegistry, plant);
    }

    @Override
    public boolean randomUpdatePlant(WorldProvider worldProvider, ClimateConditionsSystem environmentSystem, BlockEntityRegistry blockEntityRegistry, EntityRef plant) {
        // Do nothing on random update
        return false;
    }

    @Override
    public boolean isBlockOwnedByPlant(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, Vector3i block, EntityRef plant) {
        return getTreeDefinition().isBlockOwnedByPlant(worldProvider, blockEntityRegistry, block, plant);
    }

    @Override
    public Collection<Vector3i> getBlocksConnectedTo(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, Vector3i block, EntityRef plant) {
        return getTreeDefinition().getBlocksConnectedTo(worldProvider, blockEntityRegistry, block, plant);
    }
}
