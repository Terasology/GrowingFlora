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
package org.terasology.gf.generator;

import com.google.common.base.Predicate;

import org.terasology.anotherWorld.GenerationLocalParameters;
import org.terasology.anotherWorld.LocalParameters;
import org.terasology.gf.PlantType;
import org.terasology.math.ChunkMath;
import org.terasology.math.geom.Vector3i;
import org.terasology.naming.Name;
import org.terasology.registry.CoreRegistry;
import org.terasology.utilities.random.FastRandom;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;

import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
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
    public void generatePlant(long seed, CoreChunk chunk, int x, int y, int z, Region chunkRegion) {
        if (chunk.getRegion().encompasses(x, y + 1, z) && chunk.getRegion().encompasses(x, y, z)
                && groundFilter.apply(chunk.getBlock(ChunkMath.calcBlockPos(x, y, z))) && chunk.getBlock(ChunkMath.calcBlockPos(x, y + 1, z)).isPenetrable()
                && shouldSpawn(x, y, z, chunkRegion)) {
            BlockUri block = possibleBlocks.get(new FastRandom().nextInt(possibleBlocks.size()));
            Block blockToPlace = CoreRegistry.get(BlockManager.class).getBlockFamily(block).getArchetypeBlock();
            chunk.setBlock(ChunkMath.calcBlockPos(x, y + 1, z), blockToPlace);
        }
    }

    private boolean shouldSpawn(int x, int y, int z, Region chunkRegion) {
        return (spawnCondition == null || spawnCondition.apply(new GenerationLocalParameters(chunkRegion,
                new Vector3i(x, y, z))));
    }
}
