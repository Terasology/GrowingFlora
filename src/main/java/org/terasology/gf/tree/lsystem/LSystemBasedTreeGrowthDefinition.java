// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gf.tree.lsystem;

import org.joml.Vector3ic;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.generation.Region;
import org.terasology.gf.generator.ConnectedPlantGrowthDefinition;
import org.terasology.gf.util.LocalParameters;

import java.util.Collection;

public abstract class LSystemBasedTreeGrowthDefinition implements ConnectedPlantGrowthDefinition {
    protected abstract AdvancedLSystemTreeDefinition getTreeDefinition();

    protected abstract String getGeneratedBlock();

    @Override
    public final void generatePlant(long seed, Chunk chunk, int x, int y, int z, Region chunkRegion) {
        getTreeDefinition().generateTree(seed, getGeneratedBlock(), chunk, x, y, z);
    }

    @Override
    public final Long initializeGeneratedPlant(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, EntityRef plant) {
        return getTreeDefinition().setupTreeBaseBlock(worldProvider, blockEntityRegistry, plant);
    }

    @Override
    public Long initializePlantedPlant(WorldProvider worldProvider, LocalParameters localParameters, BlockEntityRegistry blockEntityRegistry, EntityRef plant) {
        return getTreeDefinition().setupPlantedSapling(plant);
    }

    @Override
    public final Long requestedUpdatePlant(WorldProvider worldProvider, LocalParameters localParameters, BlockEntityRegistry blockEntityRegistry, EntityRef plant) {
        return getTreeDefinition().updateTree(worldProvider, blockEntityRegistry, plant);
    }

    @Override
    public boolean randomUpdatePlant(WorldProvider worldProvider, LocalParameters localParameters, BlockEntityRegistry blockEntityRegistry, EntityRef plant) {
        // Do nothing on random update
        return false;
    }

    @Override
    public boolean isBlockOwnedByPlant(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, Vector3ic block, EntityRef plant) {
        return getTreeDefinition().isBlockOwnedByPlant(worldProvider, blockEntityRegistry, block, plant);
    }

    @Override
    public Collection<Vector3ic> getBlocksConnectedTo(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, Vector3ic block, EntityRef plant) {
        return getTreeDefinition().getBlocksConnectedTo(worldProvider, blockEntityRegistry, block, plant);
    }
}
