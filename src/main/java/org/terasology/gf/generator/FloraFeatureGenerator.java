// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gf.generator;

import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import org.joml.Vector3ic;
import org.terasology.biomesAPI.Biome;
import org.terasology.core.world.generator.facets.BiomeFacet;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generation.WorldRasterizer;
import org.terasology.engine.world.generator.plugin.WorldGeneratorPluginLibrary;
import org.terasology.gestalt.naming.Name;
import org.terasology.gf.PlantRegistry;
import org.terasology.gf.PlantType;
import org.terasology.engine.utilities.random.DiscreteDistribution;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FloraFeatureGenerator implements WorldRasterizer {

    private Multimap<Name, PlantSpawnDefinition> treeDefinitions = TreeMultimap.create(Ordering.natural(),
            Comparator.comparing(PlantSpawnDefinition::getPlantId));
    private Map<Name, DiscreteDistribution<PlantSpawnDefinition>> treeDefinitionsCache = new HashMap<>();

    private Multimap<Name, PlantSpawnDefinition> bushDefinitions = TreeMultimap.create(Ordering.natural(),
            Comparator.comparing(PlantSpawnDefinition::getPlantId));
    private Map<Name, DiscreteDistribution<PlantSpawnDefinition>> bushDefinitionsCache = new HashMap<>();

    private Multimap<Name, PlantSpawnDefinition> foliageDefinitions = TreeMultimap.create(Ordering.natural(),
            Comparator.comparing(PlantSpawnDefinition::getPlantId));
    private Map<Name, DiscreteDistribution<PlantSpawnDefinition>> foliageDefinitionsCache = new HashMap<>();

    public FloraFeatureGenerator() {
    }

    @Override
    public void initialize() {
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
    public void generateChunk(Chunk chunk, Region chunkRegion) {
        TreeFacet treeFacet = chunkRegion.getFacet(TreeFacet.class);
        BushFacet bushFacet = chunkRegion.getFacet(BushFacet.class);
        FoliageFacet foliageFacet = chunkRegion.getFacet(FoliageFacet.class);
        BiomeFacet biomeFacet = chunkRegion.getFacet(BiomeFacet.class);

        Set<Vector3ic> usedPositions = new HashSet<>();

        // First, generate trees, as these are the rarest ones
        for (Map.Entry<Vector3ic, Float> positionEntry : treeFacet.getWorldEntries().entrySet()) {
            Vector3ic position = positionEntry.getKey();
            float value = positionEntry.getValue();

            Biome biome = biomeFacet.getWorld(position.x(), position.z());

            long seed = Float.floatToRawIntBits(value);
            Random random = new FastRandom(seed);
            DiscreteDistribution<PlantSpawnDefinition> definitionsForBiome = getDefinitionsForBiome(biome, treeDefinitionsCache, treeDefinitions);
            PlantSpawnDefinition treeDefinition = definitionsForBiome.sample(random);
            if (treeDefinition != null && random.nextFloat() < treeDefinition.getProbability()) {
                treeDefinition.generatePlant(seed, chunk, position.x(), position.y(), position.z(), chunkRegion);
            }

            usedPositions.add(position);
        }

        // Second, generate bushes, as these are a bit more common
        for (Map.Entry<Vector3ic, Float> positionEntry : bushFacet.getWorldEntries().entrySet()) {
            Vector3ic position = positionEntry.getKey();
            if (!usedPositions.contains(position)) {
                float value = positionEntry.getValue();

                Biome biome = biomeFacet.getWorld(position.x(), position.z());

                long seed = Float.floatToRawIntBits(value);
                Random random = new FastRandom(seed);
                DiscreteDistribution<PlantSpawnDefinition> definitionsForBiome = getDefinitionsForBiome(biome, bushDefinitionsCache, bushDefinitions);
                PlantSpawnDefinition bushDefinition = definitionsForBiome.sample(random);
                if (bushDefinition != null && random.nextFloat() < bushDefinition.getProbability()) {
                    bushDefinition.generatePlant(seed, chunk, position.x(), position.y(), position.z(), chunkRegion);
                }

                usedPositions.add(position);
            }
        }

        // Third, generate grass and flowers, as these are the most common
        for (Map.Entry<Vector3ic, Float> positionEntry : foliageFacet.getWorldEntries().entrySet()) {
            Vector3ic position = positionEntry.getKey();
            if (!usedPositions.contains(position)) {
                float value = positionEntry.getValue();

                Biome biome = biomeFacet.getWorld(position.x(), position.z());

                long seed = Float.floatToRawIntBits(value);
                Random random = new FastRandom(seed);
                DiscreteDistribution<PlantSpawnDefinition> definitionsForBiome = getDefinitionsForBiome(biome, foliageDefinitionsCache, foliageDefinitions);
                PlantSpawnDefinition foliageDefinition = definitionsForBiome.sample(random);
                if (foliageDefinition != null && random.nextFloat() < foliageDefinition.getProbability()) {
                    foliageDefinition.generatePlant(seed, chunk, position.x(), position.y(), position.z(), chunkRegion);
                }

                usedPositions.add(position);
            }
        }
    }

    private DiscreteDistribution<PlantSpawnDefinition> getDefinitionsForBiome(Biome biome, Map<Name,
            DiscreteDistribution<PlantSpawnDefinition>> cache, Multimap<Name, PlantSpawnDefinition> definitions) {
        DiscreteDistribution<PlantSpawnDefinition> result = cache.get(biome.getId());
        if (result != null) {
            return result;
        }

        result = new DiscreteDistribution<>();
        for (PlantSpawnDefinition floraDefinition : definitions.get(biome.getId())) {
            result.add(floraDefinition, floraDefinition.getRarity());
        }
        cache.put(biome.getId(), result);
        return result;
    }
}
