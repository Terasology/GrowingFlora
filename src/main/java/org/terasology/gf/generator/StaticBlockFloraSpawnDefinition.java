// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gf.generator;

import com.google.common.base.Predicate;
import org.joml.Vector3i;
import org.terasology.gf.util.GenerationLocalParameters;
import org.terasology.gf.util.LocalParameters;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.BlockUri;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.generation.Region;
import org.terasology.gf.PlantType;
import org.terasology.gestalt.naming.Name;

import java.util.List;

public abstract class StaticBlockFloraSpawnDefinition implements PlantSpawnDefinition {
    private PlantType plantType;
    private Name biomeId;
    private float rarity;
    private float probability;
    private String plantId;
    private List<BlockUri> possibleBlocks;
    private Predicate<Block> groundFilter;
    private Predicate<LocalParameters> spawnCondition;

    public StaticBlockFloraSpawnDefinition(PlantType plantType, String biomeId, float rarity, float probability, String plantId,
                                           List<BlockUri> possibleBlocks, Predicate<Block> groundFilter, Predicate<LocalParameters> spawnCondition) {
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
    public void generatePlant(long seed, Chunk chunk, int x, int y, int z, Region chunkRegion) {
        if (chunk.getRegion().contains(x, y + 1, z) && chunk.getRegion().contains(x, y, z)
                && groundFilter.apply(chunk.getBlock(Chunks.toRelative(x, y, z, new Vector3i()))) && chunk.getBlock(Chunks.toRelative(x, y + 1, z, new Vector3i())).isPenetrable()
                && shouldSpawn(x, y, z, chunkRegion)) {
            BlockUri block = possibleBlocks.get(new FastRandom().nextInt(possibleBlocks.size()));
            Block blockToPlace = CoreRegistry.get(BlockManager.class).getBlockFamily(block).getArchetypeBlock();
            chunk.setBlock(Chunks.toRelative(x, y + 1, z, new Vector3i()), blockToPlace);
        }
    }

    private boolean shouldSpawn(int x, int y, int z, Region chunkRegion) {
        return (spawnCondition == null || spawnCondition.apply(new GenerationLocalParameters(chunkRegion,
                new Vector3i(x, y, z))));
    }
}
