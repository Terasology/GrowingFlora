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

import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import org.terasology.anotherWorld.Biome;
import org.terasology.anotherWorld.BiomeRegistry;
import org.terasology.anotherWorld.FeatureGenerator;
import org.terasology.anotherWorld.generation.BiomeFacet;
import org.terasology.anotherWorld.util.ChanceRandomizer;
import org.terasology.anotherWorld.util.PDist;
import org.terasology.gf.PlantRegistry;
import org.terasology.gf.PlantType;
import org.terasology.math.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generator.plugin.WorldGeneratorPluginLibrary;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class FloraFeatureGenerator implements FeatureGenerator {
    private PDist treeTriesPerChunk;
    private PDist bushTriesPerChunk;
    private PDist foliageTriesPerChunk;

    private Multimap<String, PlantSpawnDefinition> treeDefinitions = TreeMultimap.create(Ordering.natural(),
            new Comparator<PlantSpawnDefinition>() {
                @Override
                public int compare(PlantSpawnDefinition o1, PlantSpawnDefinition o2) {
                    return o1.getPlantId().compareTo(o2.getPlantId());
                }
            });
    private Map<String, ChanceRandomizer<PlantSpawnDefinition>> treeDefinitionsCache = new HashMap<>();

    private Multimap<String, PlantSpawnDefinition> bushDefinitions = TreeMultimap.create(Ordering.natural(),
            new Comparator<PlantSpawnDefinition>() {
                @Override
                public int compare(PlantSpawnDefinition o1, PlantSpawnDefinition o2) {
                    return o1.getPlantId().compareTo(o2.getPlantId());
                }
            });
    private Map<String, ChanceRandomizer<PlantSpawnDefinition>> bushDefinitionsCache = new HashMap<>();

    private Multimap<String, PlantSpawnDefinition> foliageDefinitions = TreeMultimap.create(Ordering.natural(),
            new Comparator<PlantSpawnDefinition>() {
                @Override
                public int compare(PlantSpawnDefinition o1, PlantSpawnDefinition o2) {
                    return o1.getPlantId().compareTo(o2.getPlantId());
                }
            });
    private Map<String, ChanceRandomizer<PlantSpawnDefinition>> foliageDefinitionsCache = new HashMap<>();

    public FloraFeatureGenerator(PDist treeTriesPerChunk, PDist bushTriesPerChunk, PDist foliageTriesPerChunk) {
        this.treeTriesPerChunk = treeTriesPerChunk;
        this.bushTriesPerChunk = bushTriesPerChunk;
        this.foliageTriesPerChunk = foliageTriesPerChunk;
        loadPlantGrowthDefinitions();
        loadPlantSpawnDefinition();

    }

    private void loadPlantGrowthDefinitions() {
        PlantRegistry plantRegistry = CoreRegistry.get(PlantRegistry.class);

        WorldGeneratorPluginLibrary pluginLibrary = CoreRegistry.get(WorldGeneratorPluginLibrary.class);
        List<PlantGrowthDefinition> plantGrowthDefinitions = pluginLibrary.instantiateAllOfType(PlantGrowthDefinition.class);
        for (PlantGrowthDefinition plantGrowthDefinition : plantGrowthDefinitions) {
            plantRegistry.addPlantType(plantGrowthDefinition.getPlantId(), plantGrowthDefinition);
        }
    }

    private void loadPlantSpawnDefinition() {
        WorldGeneratorPluginLibrary pluginLibrary = CoreRegistry.get(WorldGeneratorPluginLibrary.class);
        List<PlantSpawnDefinition> plantSpawnDefinitions = pluginLibrary.instantiateAllOfType(PlantSpawnDefinition.class);
        for (PlantSpawnDefinition plantSpawnDefinition : plantSpawnDefinitions) {
            PlantType plantType = plantSpawnDefinition.getPlantType();
            if (plantType == PlantType.TREE) {
                treeDefinitions.put(plantSpawnDefinition.getBiomeId(), plantSpawnDefinition);
            } else if (plantType == PlantType.BUSH) {
                bushDefinitions.put(plantSpawnDefinition.getBiomeId(), plantSpawnDefinition);
            } else if (plantType == PlantType.GRASS) {
                foliageDefinitions.put(plantSpawnDefinition.getBiomeId(), plantSpawnDefinition);
            }
        }
    }


    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        FloraFacet floraFacet = chunkRegion.getFacet(FloraFacet.class);
        BiomeFacet biomeFacet = chunkRegion.getFacet(BiomeFacet.class);
        BiomeRegistry biomeRegistry = CoreRegistry.get(BiomeRegistry.class);

        for (Vector3i position : chunk.getRegion()) {
            float floraValue = floraFacet.getWorld(position);

            if (floraValue == 0) {
                // there is nothing to do here
                continue;
            }

            Random random = new FastRandom((long) floraValue);
            Biome biome = biomeFacet.getWorld(position.x, position.z);

            // First, generate trees, as these are the rarest ones
            float treeTries = treeTriesPerChunk.getIntValue(random) / 256f;
            if (random.nextFloat() < treeTries) {
                ChanceRandomizer<PlantSpawnDefinition> definitionsForBiome = getDefinitionsForBiome(biome, biomeRegistry, treeDefinitionsCache, treeDefinitions);
                PlantSpawnDefinition treeDefinition = definitionsForBiome.randomizeObject(random);
                if (treeDefinition != null && random.nextFloat() < treeDefinition.getProbability()) {
                    treeDefinition.generatePlant(random.nextLong(), chunk, position.x, position.y, position.z, chunkRegion);
                }
            }

            // Second, generate bushes, as these are a bit more common
            float bushTries = bushTriesPerChunk.getIntValue(random) / 256f;
            if (random.nextFloat() < bushTries) {
                ChanceRandomizer<PlantSpawnDefinition> definitionsForBiome = getDefinitionsForBiome(biome, biomeRegistry, bushDefinitionsCache, bushDefinitions);
                PlantSpawnDefinition bushDefinition = definitionsForBiome.randomizeObject(random);
                if (bushDefinition != null && random.nextFloat() < bushDefinition.getProbability()) {
                    bushDefinition.generatePlant(random.nextLong(), chunk, position.x, position.y, position.z, chunkRegion);
                }
            }

            // Third, generate grass and flowers, as these are the most common
            float foliageTries = foliageTriesPerChunk.getIntValue(random) / 256f;
            if (random.nextFloat() < foliageTries) {
                ChanceRandomizer<PlantSpawnDefinition> definitionsForBiome = getDefinitionsForBiome(biome, biomeRegistry, foliageDefinitionsCache, foliageDefinitions);
                PlantSpawnDefinition foliageDefinition = definitionsForBiome.randomizeObject(random);
                if (foliageDefinition != null && random.nextFloat() < foliageDefinition.getProbability()) {
                    foliageDefinition.generatePlant(random.nextLong(), chunk, position.x, position.y, position.z, chunkRegion);
                }
            }
        }
    }

    private ChanceRandomizer<PlantSpawnDefinition> getDefinitionsForBiome(
            Biome biome, BiomeRegistry biomeRegistry,
            Map<String, ChanceRandomizer<PlantSpawnDefinition>> cache, Multimap<String, PlantSpawnDefinition> definitions) {
        ChanceRandomizer<PlantSpawnDefinition> result = cache.get(biome.getBiomeId());
        if (result != null) {
            return result;
        }

        result = new ChanceRandomizer<>(100);
        Biome biomeToAdd = biome;
        while (biomeToAdd != null) {
            for (PlantSpawnDefinition floraDefinition : definitions.get(biome.getBiomeId())) {
                result.addChance(floraDefinition.getRarity(), floraDefinition);
            }
            biomeToAdd = biomeRegistry.getBiomeById(biomeToAdd.getBiomeParent());
        }
        result.initialize();
        cache.put(biome.getBiomeId(), result);
        return result;
    }
}
