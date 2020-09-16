// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.growingflora.generator;

import com.google.common.base.Predicate;
import org.terasology.anotherWorld.GenerationLocalParameters;
import org.terasology.anotherWorld.LocalParameters;
import org.terasology.engine.math.ChunkMath;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.BlockUri;
import org.terasology.engine.world.chunks.CoreChunk;
import org.terasology.engine.world.generation.Region;
import org.terasology.gestalt.naming.Name;
import org.terasology.growingflora.PlantType;
import org.terasology.math.geom.Vector3i;

import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public abstract class StaticBlockFloraSpawnDefinition implements PlantSpawnDefinition {
    private final PlantType plantType;
    private final Name biomeId;
    private final float rarity;
    private final float probability;
    private final String plantId;
    private final List<BlockUri> possibleBlocks;
    private final Predicate<Block> groundFilter;
    private final Predicate<LocalParameters> spawnCondition;

    public StaticBlockFloraSpawnDefinition(PlantType plantType, String biomeId, float rarity, float probability,
                                           String plantId,
                                           List<BlockUri> possibleBlocks, Predicate<Block> groundFilter,
                                           Predicate<LocalParameters> spawnCondition) {
        this.plantType = plantType;
        this.biomeId = new Name(biomeId);
        this.rarity = rarity;
        this.probability = probability;
        this.plantId = plantId;
        this.possibleBlocks = possibleBlocks;
        this.groundFilter = groundFilter;
        this.spawnCondition = spawnCondition;
    }

    @Override
    public PlantType getPlantType() {
        return plantType;
    }

    @Override
    public Name getBiomeId() {
        return biomeId;
    }

    @Override
    public float getRarity() {
        return rarity;
    }

    @Override
    public float getProbability() {
        return probability;
    }

    @Override
    public String getPlantId() {
        return plantId;
    }

    @Override
    public void generatePlant(long seed, CoreChunk chunk, int x, int y, int z, Region chunkRegion) {
        if (chunk.getRegion().encompasses(x, y + 1, z) && chunk.getRegion().encompasses(x, y, z)
                && groundFilter.apply(chunk.getBlock(ChunkMath.calcRelativeBlockPos(x, y, z))) && chunk.getBlock(ChunkMath.calcRelativeBlockPos(x, y + 1, z)).isPenetrable()
                && shouldSpawn(x, y, z, chunkRegion)) {
            BlockUri block = possibleBlocks.get(new FastRandom().nextInt(possibleBlocks.size()));
            Block blockToPlace = CoreRegistry.get(BlockManager.class).getBlockFamily(block).getArchetypeBlock();
            chunk.setBlock(ChunkMath.calcRelativeBlockPos(x, y + 1, z), blockToPlace);
        }
    }

    private boolean shouldSpawn(int x, int y, int z, Region chunkRegion) {
        return (spawnCondition == null || spawnCondition.apply(new GenerationLocalParameters(chunkRegion,
                new Vector3i(x, y, z))));
    }
}
