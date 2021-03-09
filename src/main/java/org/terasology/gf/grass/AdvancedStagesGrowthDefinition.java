// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gf.grass;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.joml.Vector3i;
import org.terasology.anotherWorld.EnvironmentLocalParameters;
import org.terasology.anotherWorld.GenerationLocalParameters;
import org.terasology.anotherWorld.LocalParameters;
import org.terasology.climateConditions.ClimateConditionsSystem;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.BlockUri;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.chunks.CoreChunk;
import org.terasology.engine.world.generation.Region;
import org.terasology.gf.generator.PlantGrowthDefinition;

import java.util.List;

public class AdvancedStagesGrowthDefinition implements PlantGrowthDefinition {
    private String plantId;
    private Predicate<LocalParameters> spawnCondition;
    private Function<LocalParameters, Long> growthTimeFunction;
    private List<BlockUri> plantStages;
    private Predicate<LocalParameters> deathCondition;
    private BlockUri deadPlantBlock;

    public AdvancedStagesGrowthDefinition(String plantId, Predicate<LocalParameters> spawnCondition,
                                          Function<LocalParameters, Long> growthTimeFunction, List<BlockUri> plantStages,
                                          Predicate<LocalParameters> deathCondition, BlockUri deadPlantBlock) {
        this.plantId = plantId;
        this.spawnCondition = spawnCondition;
        this.growthTimeFunction = growthTimeFunction;
        this.plantStages = plantStages;
        this.deathCondition = deathCondition;
        this.deadPlantBlock = deadPlantBlock;
    }

    @Override
    public String getPlantId() {
        return plantId;
    }

    @Override
    public void generatePlant(long seed, CoreChunk chunk, int x, int y, int z, Region chunkRegion) {
        if (chunk.getRegion().contains(x, y, z) && shouldSpawn(chunkRegion, x, y, z)) {
            BlockManager blockManager = CoreRegistry.get(BlockManager.class);
            Block lastBlock = blockManager.getBlock(plantStages.get(plantStages.size() - 1));
            chunk.setBlock(Chunks.toRelative(x, y, z, new Vector3i()), lastBlock);
        }
    }

    private boolean shouldSpawn(Region chunkRegion, int x, int y, int z) {
        return spawnCondition == null || spawnCondition.apply(new GenerationLocalParameters(chunkRegion,
                new Vector3i(x, y, z)));
    }

    @Override
    public Long initializeGeneratedPlant(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, EntityRef plant) {
        return null;
    }

    @Override
    public Long initializePlantedPlant(WorldProvider worldProvider, ClimateConditionsSystem environmentSystem, BlockEntityRegistry blockEntityRegistry, EntityRef plant) {
        BlockComponent block = plant.getComponent(BlockComponent.class);
        Vector3i position = block.getPosition(new Vector3i());
        return growthTimeFunction.apply(new EnvironmentLocalParameters(environmentSystem, position));
    }

    @Override
    public Long requestedUpdatePlant(WorldProvider worldProvider, ClimateConditionsSystem environmentSystem, BlockEntityRegistry blockEntityRegistry, EntityRef plant) {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        BlockComponent block = plant.getComponent(BlockComponent.class);
        Vector3i position = block.getPosition(new Vector3i());

        if (shouldDie(environmentSystem, position)) {
            replaceBlock(worldProvider, blockManager, plant, position, deadPlantBlock, true);

            return null;
        } else {
            int currentIndex = plantStages.indexOf(block.getBlock().getURI());

            int nextIndex = currentIndex + 1;
            BlockUri nextStage = plantStages.get(nextIndex);
            final boolean hasMoreStages = nextIndex < plantStages.size() - 1;

            replaceBlock(worldProvider, blockManager, plant, position, nextStage, !hasMoreStages);

            if (hasMoreStages) {
                return growthTimeFunction.apply(new EnvironmentLocalParameters(environmentSystem, position));
            } else {
                // Entered the last phase
                return null;
            }
        }
    }

    @Override
    public boolean randomUpdatePlant(WorldProvider worldProvider, ClimateConditionsSystem environmentSystem, BlockEntityRegistry blockEntityRegistry, EntityRef plant) {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        BlockComponent block = plant.getComponent(BlockComponent.class);
        Vector3i position = block.getPosition(new Vector3i());

        if (shouldDie(environmentSystem, position)) {
            replaceBlock(worldProvider, blockManager, plant, position, deadPlantBlock, true);

            return true;
        }
        return false;
    }

    protected void replaceBlock(WorldProvider worldProvider, BlockManager blockManager, EntityRef plant, Vector3i position, BlockUri nextStage, boolean isLast) {
        worldProvider.setBlock(position, blockManager.getBlock(nextStage));
    }

    private boolean shouldDie(ClimateConditionsSystem environmentSystem, Vector3i position) {
        return deathCondition != null && deathCondition.apply(new EnvironmentLocalParameters(environmentSystem, position));
    }
}
