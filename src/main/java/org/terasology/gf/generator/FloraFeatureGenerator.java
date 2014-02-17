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
import org.terasology.anotherWorld.BiomeProvider;
import org.terasology.anotherWorld.FeatureGenerator;
import org.terasology.anotherWorld.GenerationParameters;
import org.terasology.anotherWorld.util.ChanceRandomizer;
import org.terasology.anotherWorld.util.ChunkRandom;
import org.terasology.anotherWorld.util.PDist;
import org.terasology.gf.PlantRegistry;
import org.terasology.gf.PlantType;
import org.terasology.math.Vector2i;
import org.terasology.math.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.utilities.random.Random;
import org.terasology.world.ChunkView;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.generator.plugin.WorldGeneratorPluginLibrary;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class FloraFeatureGenerator implements FeatureGenerator {
    private String seed;

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
    }

    @Override
    public void initializeWithSeed(String worldSeed) {
        this.seed = worldSeed;
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
    public void generateInChunk(Vector3i chunkPos, ChunkView view, GenerationParameters generationParameters) {
        Random random = ChunkRandom.getChunkRandom(seed, chunkPos, 787234);

        int chunkStartX = chunkPos.x * ChunkConstants.SIZE_X;
        int chunkStartZ = chunkPos.z * ChunkConstants.SIZE_Z;

        // First, generate trees, as these are the rarest ones
        int treeTries = treeTriesPerChunk.getIntValue(random);
        for (int i = 0; i < treeTries; i++) {
            int x = random.nextInt(ChunkConstants.SIZE_X);
            int z = random.nextInt(ChunkConstants.SIZE_Z);

            int groundLevel = generationParameters.getLandscapeProvider().getHeight(new Vector2i(chunkStartX + x, chunkStartZ + z));

            BiomeProvider biomeProvider = generationParameters.getBiomeProvider();
            Biome biome = biomeProvider.getBiomeAt(chunkStartX + x, groundLevel, chunkStartZ + z);
            ChanceRandomizer<PlantSpawnDefinition> definitionsForBiome = getDefinitionsForBiome(biome, biomeProvider, treeDefinitionsCache, treeDefinitions);
            PlantSpawnDefinition treeDefinition = definitionsForBiome.randomizeObject(random);
            if (treeDefinition != null && random.nextFloat() < treeDefinition.getProbability()) {
                treeDefinition.generatePlant(seed, chunkPos, view, x, groundLevel, z, generationParameters);
            }
        }

        // Second, generate bushes, as these are a bit more common
        int bushTries = bushTriesPerChunk.getIntValue(random);
        for (int i = 0; i < bushTries; i++) {
            int x = random.nextInt(ChunkConstants.SIZE_X);
            int z = random.nextInt(ChunkConstants.SIZE_Z);

            int groundLevel = generationParameters.getLandscapeProvider().getHeight(new Vector2i(chunkStartX + x, chunkStartZ + z));

            BiomeProvider biomeProvider = generationParameters.getBiomeProvider();
            Biome biome = biomeProvider.getBiomeAt(chunkStartX + x, groundLevel, chunkStartZ + z);
            ChanceRandomizer<PlantSpawnDefinition> definitionsForBiome = getDefinitionsForBiome(biome, biomeProvider, bushDefinitionsCache, bushDefinitions);
            PlantSpawnDefinition bushDefinition = definitionsForBiome.randomizeObject(random);
            if (bushDefinition != null && random.nextFloat() < bushDefinition.getProbability()) {
                bushDefinition.generatePlant(seed, chunkPos, view, x, groundLevel, z, generationParameters);
            }
        }

        // Third, generate grass and flowers, as these are the most common
        int foliageTries = foliageTriesPerChunk.getIntValue(random);
        for (int i = 0; i < foliageTries; i++) {
            int x = random.nextInt(ChunkConstants.SIZE_X);
            int z = random.nextInt(ChunkConstants.SIZE_Z);

            int groundLevel = generationParameters.getLandscapeProvider().getHeight(new Vector2i(chunkStartX + x, chunkStartZ + z));

            BiomeProvider biomeProvider = generationParameters.getBiomeProvider();
            Biome biome = biomeProvider.getBiomeAt(chunkStartX + x, groundLevel, chunkStartZ + z);
            ChanceRandomizer<PlantSpawnDefinition> definitionsForBiome = getDefinitionsForBiome(biome, biomeProvider, foliageDefinitionsCache, foliageDefinitions);
            PlantSpawnDefinition foliageDefinition = definitionsForBiome.randomizeObject(random);
            if (foliageDefinition != null && random.nextFloat() < foliageDefinition.getProbability()) {
                foliageDefinition.generatePlant(seed, chunkPos, view, x, groundLevel, z, generationParameters);
            }
        }
    }

    private ChanceRandomizer<PlantSpawnDefinition> getDefinitionsForBiome(
            Biome biome, BiomeProvider biomeProvider,
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
            biomeToAdd = biomeProvider.getBiomeById(biomeToAdd.getBiomeParent());
        }
        result.initialize();
        cache.put(biome.getBiomeId(), result);
        return result;
    }
}
