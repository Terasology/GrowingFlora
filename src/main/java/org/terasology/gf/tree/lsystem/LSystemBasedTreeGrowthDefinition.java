/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.gf.tree.lsystem;

import org.terasology.climateConditions.ClimateConditionsSystem;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.gf.generator.ConnectedPlantGrowthDefinition;
import org.terasology.math.Vector3i;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;

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
    public final Long initializeGeneratedPlant(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, EntityRef plant) {
        return getTreeDefinition().setupTreeBaseBlock(worldProvider, blockEntityRegistry, plant);
    }

    @Override
    public Long initializePlantedPlant(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, EntityRef plant) {
        return getTreeDefinition().setupPlantedSapling(plant);
    }

    @Override
    public final Long updatePlant(WorldProvider worldProvider, ClimateConditionsSystem environmentSystem, BlockEntityRegistry blockEntityRegistry, EntityRef plant) {
        return getTreeDefinition().updateTree(worldProvider, blockEntityRegistry, plant);
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
