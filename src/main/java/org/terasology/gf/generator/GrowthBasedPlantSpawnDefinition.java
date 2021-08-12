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

import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.generation.Region;
import org.terasology.gf.PlantRegistry;
import org.terasology.gf.PlantType;
import org.terasology.gestalt.naming.Name;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public abstract class GrowthBasedPlantSpawnDefinition implements PlantSpawnDefinition {
    private PlantType plantType;
    private String plantId;
    private Name biomeId;
    private float rarity;
    private float probability;

    public GrowthBasedPlantSpawnDefinition(PlantType plantType, String plantId, String biomeId, float rarity, float probability) {
        this.plantType = plantType;
        this.plantId = plantId;
        this.biomeId = new Name(biomeId);
        this.rarity = rarity;
        this.probability = probability;
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
    public void generatePlant(long seed, Chunk chunk, int x, int y, int z, Region region) {
        PlantRegistry plantRegistry = CoreRegistry.get(PlantRegistry.class);
        PlantGrowthDefinition plantGrowthDefinition = plantRegistry.getPlantGrowthDefinition(plantId);
        plantGrowthDefinition.generatePlant(seed, chunk, x, y + 1, z, region);
    }
}
