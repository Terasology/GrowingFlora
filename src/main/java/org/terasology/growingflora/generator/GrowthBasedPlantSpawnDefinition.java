// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.growingflora.generator;

import com.google.common.base.Predicate;
import org.terasology.engine.math.ChunkMath;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.chunks.CoreChunk;
import org.terasology.engine.world.generation.Region;
import org.terasology.gestalt.naming.Name;
import org.terasology.growingflora.PlantRegistry;
import org.terasology.growingflora.PlantType;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public abstract class GrowthBasedPlantSpawnDefinition implements PlantSpawnDefinition {
    private final PlantType plantType;
    private final String plantId;
    private final Name biomeId;
    private final float rarity;
    private final float probability;
    private final Predicate<Block> groundFilter;

    public GrowthBasedPlantSpawnDefinition(PlantType plantType, String plantId, String biomeId, float rarity,
                                           float probability, Predicate<Block> groundFilter) {
        this.plantType = plantType;
        this.plantId = plantId;
        this.biomeId = new Name(biomeId);
        this.rarity = rarity;
        this.probability = probability;
        this.groundFilter = groundFilter;
    }

    @Override
    public String getPlantId() {
        return plantId;
    }

    @Override
    public Name getBiomeId() {
        return biomeId;
    }

    @Override
    public PlantType getPlantType() {
        return plantType;
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
    public void generatePlant(long seed, CoreChunk chunk, int x, int y, int z, Region region) {
        if (chunk.getRegion().encompasses(x, y + 1, z) && chunk.getRegion().encompasses(x, y, z)
                && groundFilter.apply(chunk.getBlock(ChunkMath.calcRelativeBlockPos(x, y, z)))
                && chunk.getBlock(ChunkMath.calcRelativeBlockPos(x, y + 1, z)).isPenetrable()) {
            PlantRegistry plantRegistry = CoreRegistry.get(PlantRegistry.class);
            PlantGrowthDefinition plantGrowthDefinition = plantRegistry.getPlantGrowthDefinition(plantId);
            plantGrowthDefinition.generatePlant(seed, chunk, x, y + 1, z, region);
        }
    }
}
