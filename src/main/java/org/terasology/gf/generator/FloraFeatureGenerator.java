// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gf.generator;

import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import org.terasology.anotherWorld.AnotherWorldBiome;
import org.terasology.anotherWorld.FeatureGenerator;
import org.terasology.anotherWorld.generation.BiomeFacet;
import org.terasology.anotherWorld.util.ChanceRandomizer;
import org.terasology.biomesAPI.BiomeRegistry;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;
import org.terasology.engine.world.chunks.CoreChunk;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generator.plugin.WorldGeneratorPluginLibrary;
import org.terasology.gestalt.naming.Name;
import org.terasology.gf.PlantRegistry;
import org.terasology.gf.PlantType;
import org.terasology.math.geom.Vector3i;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class FloraFeatureGenerator implements FeatureGenerator {

    private final Multimap<Name, PlantSpawnDefinition> treeDefinitions = TreeMultimap.create(Ordering.natural(),
            new Comparator<PlantSpawnDefinition>() {
                @Override
                public int compare(PlantSpawnDefinition o1, PlantSpawnDefinition o2) {
                    return o1.getPlantId().compareTo(o2.getPlantId());
                }
            });
    private final Map<Name, ChanceRandomizer<PlantSpawnDefinition>> treeDefinitionsCache = new HashMap<>();

    private final Multimap<Name, PlantSpawnDefinition> bushDefinitions = TreeMultimap.create(Ordering.natural(),
            new Comparator<PlantSpawnDefinition>() {
                @Override
                public int compare(PlantSpawnDefinition o1, PlantSpawnDefinition o2) {
                    return o1.getPlantId().compareTo(o2.getPlantId());
                }
            });
    private final Map<Name, ChanceRandomizer<PlantSpawnDefinition>> bushDefinitionsCache = new HashMap<>();

    private final Multimap<Name, PlantSpawnDefinition> foliageDefinitions = TreeMultimap.create(Ordering.natural(),
            new Comparator<PlantSpawnDefinition>() {
                @Override
                public int compare(PlantSpawnDefinition o1, PlantSpawnDefinition o2) {
                    return o1.getPlantId().compareTo(o2.getPlantId());
                }
            });
    private final Map<Name, ChanceRandomizer<PlantSpawnDefinition>> foliageDefinitionsCache = new HashMap<>();

    public FloraFeatureGenerator() {
        loadPlantGrowthDefinitions();
        loadPlantSpawnDefinition();

    }

    @Override
    public void initialize() {
    }

    private void loadPlantGrowthDefinitions() {
        PlantRegistry plantRegistry = CoreRegistry.get(PlantRegistry.class);

        WorldGeneratorPluginLibrary pluginLibrary = CoreRegistry.get(WorldGeneratorPluginLibrary.class);
        List<PlantGrowthDefinition> plantGrowthDefinitions =
                pluginLibrary.instantiateAllOfType(PlantGrowthDefinition.class);
        for (PlantGrowthDefinition plantGrowthDefinition : plantGrowthDefinitions) {
            plantRegistry.addPlantType(plantGrowthDefinition.getPlantId(), plantGrowthDefinition);
        }
    }

    private void loadPlantSpawnDefinition() {
        WorldGeneratorPluginLibrary pluginLibrary = CoreRegistry.get(WorldGeneratorPluginLibrary.class);
        List<PlantSpawnDefinition> plantSpawnDefinitions =
                pluginLibrary.instantiateAllOfType(PlantSpawnDefinition.class);
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
        TreeFacet treeFacet = chunkRegion.getFacet(TreeFacet.class);
        BushFacet bushFacet = chunkRegion.getFacet(BushFacet.class);
        FoliageFacet foliageFacet = chunkRegion.getFacet(FoliageFacet.class);
        BiomeFacet biomeFacet = chunkRegion.getFacet(BiomeFacet.class);
        BiomeRegistry biomeRegistry = CoreRegistry.get(BiomeRegistry.class);

        Set<Vector3i> usedPositions = new HashSet<>();

        // First, generate trees, as these are the rarest ones
        for (Map.Entry<Vector3i, Float> positionEntry : treeFacet.getFlaggedPositions().entrySet()) {
            Vector3i position = positionEntry.getKey();
            float value = positionEntry.getValue();

            AnotherWorldBiome biome = biomeFacet.getWorld(position.x, position.z);

            long seed = Float.floatToRawIntBits(value);
            Random random = new FastRandom(seed);
            ChanceRandomizer<PlantSpawnDefinition> definitionsForBiome = getDefinitionsForBiome(biome, biomeRegistry,
                    treeDefinitionsCache, treeDefinitions);
            PlantSpawnDefinition treeDefinition = definitionsForBiome.randomizeObject(random);
            if (treeDefinition != null && random.nextFloat() < treeDefinition.getProbability()) {
                treeDefinition.generatePlant(seed, chunk, position.x, position.y, position.z, chunkRegion);
            }

            usedPositions.add(position);
        }

        // Second, generate bushes, as these are a bit more common
        for (Map.Entry<Vector3i, Float> positionEntry : bushFacet.getFlaggedPositions().entrySet()) {
            Vector3i position = positionEntry.getKey();
            if (!usedPositions.contains(position)) {
                float value = positionEntry.getValue();

                AnotherWorldBiome biome = biomeFacet.getWorld(position.x, position.z);

                long seed = Float.floatToRawIntBits(value);
                Random random = new FastRandom(seed);
                ChanceRandomizer<PlantSpawnDefinition> definitionsForBiome = getDefinitionsForBiome(biome,
                        biomeRegistry, bushDefinitionsCache, bushDefinitions);
                PlantSpawnDefinition bushDefinition = definitionsForBiome.randomizeObject(random);
                if (bushDefinition != null && random.nextFloat() < bushDefinition.getProbability()) {
                    bushDefinition.generatePlant(seed, chunk, position.x, position.y, position.z, chunkRegion);
                }

                usedPositions.add(position);
            }
        }

        // Third, generate grass and flowers, as these are the most common
        for (Map.Entry<Vector3i, Float> positionEntry : foliageFacet.getFlaggedPositions().entrySet()) {
            Vector3i position = positionEntry.getKey();
            if (!usedPositions.contains(position)) {
                float value = positionEntry.getValue();

                AnotherWorldBiome biome = biomeFacet.getWorld(position.x, position.z);

                long seed = Float.floatToRawIntBits(value);
                Random random = new FastRandom(seed);
                ChanceRandomizer<PlantSpawnDefinition> definitionsForBiome = getDefinitionsForBiome(biome,
                        biomeRegistry, foliageDefinitionsCache, foliageDefinitions);
                PlantSpawnDefinition foliageDefinition = definitionsForBiome.randomizeObject(random);
                if (foliageDefinition != null && random.nextFloat() < foliageDefinition.getProbability()) {
                    foliageDefinition.generatePlant(seed, chunk, position.x, position.y, position.z, chunkRegion);
                }

                usedPositions.add(position);
            }
        }
    }

    private ChanceRandomizer<PlantSpawnDefinition> getDefinitionsForBiome(
            AnotherWorldBiome biome, BiomeRegistry biomeRegistry,
            Map<Name, ChanceRandomizer<PlantSpawnDefinition>> cache, Multimap<Name, PlantSpawnDefinition> definitions) {
        ChanceRandomizer<PlantSpawnDefinition> result = cache.get(biome.getId());
        if (result != null) {
            return result;
        }

        result = new ChanceRandomizer<>(100);
        AnotherWorldBiome biomeToAdd = biome;
        boolean parentFound = true;
        while (parentFound) {
            parentFound = false;
            for (PlantSpawnDefinition floraDefinition : definitions.get(biome.getId())) {
                result.addChance(floraDefinition.getRarity(), floraDefinition);
            }
            for (AnotherWorldBiome biome1 : biomeRegistry.getRegisteredBiomes(AnotherWorldBiome.class)) {
                if (biomeToAdd.getBiomeParent().equals(biome1.getId())) {
                    biomeToAdd = biome1;
                    parentFound = true;
                    break;
                }
            }
        }
        result.initialize();
        cache.put(biome.getId(), result);
        return result;
    }
}
