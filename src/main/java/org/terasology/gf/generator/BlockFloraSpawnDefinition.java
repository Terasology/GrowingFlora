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

import org.terasology.anotherWorld.GenerationParameters;
import org.terasology.anotherWorld.util.Filter;
import org.terasology.gf.PlantRegistry;
import org.terasology.gf.PlantType;
import org.terasology.math.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.ChunkView;
import org.terasology.world.block.Block;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public abstract class BlockFloraSpawnDefinition implements PlantSpawnDefinition {
    private PlantType plantType;
    private String plantId;
    private String biomeId;
    private float rarity;
    private float probability;
    private Filter<Block> groundFilter;

    public BlockFloraSpawnDefinition(PlantType plantType, String plantId, String biomeId, float rarity, float probability, Filter<Block> groundFilter) {
        this.plantType = plantType;
        this.plantId = plantId;
        this.biomeId = biomeId;
        this.rarity = rarity;
        this.probability = probability;
        this.groundFilter = groundFilter;
    }

    @Override
    public String getBiomeId() {
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
    public void generatePlant(String seed, Vector3i chunkPos, ChunkView chunkView, int x, int y, int z, GenerationParameters generationParameters) {
        if (groundFilter.accepts(chunkView.getBlock(x, y, z))) {
            PlantRegistry plantRegistry = CoreRegistry.get(PlantRegistry.class);
            PlantGrowthDefinition plantGrowthDefinition = plantRegistry.getPlantGrowthDefinition(plantId);
            plantGrowthDefinition.generatePlant(seed, chunkPos, chunkView, x, y, z, generationParameters);
        }
    }
}
