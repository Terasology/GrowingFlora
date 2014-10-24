package org.terasology.gf.grass;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.terasology.anotherWorld.GenerationLocalParameters;
import org.terasology.anotherWorld.LocalParameters;
import org.terasology.anotherWorld.EnvironmentLocalParameters;
import org.terasology.anotherWorld.environment.EnvironmentSystem;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.gf.generator.PlantGrowthDefinition;
import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.utilities.random.FastRandom;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class ReplaceBlockGrowthDefinition implements PlantGrowthDefinition {
    private String plantId;
    private List<BlockUri> plantStages;
    private List<Long> growthIntervals;
    private Predicate<LocalParameters> spawnCondition;
    private Function<LocalParameters, Float> growthChance;

    public ReplaceBlockGrowthDefinition(String plantId, List<BlockUri> plantStages, long growthInterval, long penultimateGrowthInterval,
                                        Predicate<LocalParameters> spawnCondition, Function<LocalParameters, Float> growthChance) {
        this.plantId = plantId;
        this.plantStages = plantStages;
        growthIntervals = new ArrayList<>();
        for (int i = 0; i < plantStages.size() - 2; i++) {
            growthIntervals.add(growthInterval);
        }
        growthIntervals.add(penultimateGrowthInterval);
        this.spawnCondition = spawnCondition;
        this.growthChance = growthChance;
    }

    public ReplaceBlockGrowthDefinition(String plantId, List<BlockUri> plantStages, long growthInterval,
                                        Predicate<LocalParameters> spawnCondition, Function<LocalParameters, Float> growthChance) {
        this.plantId = plantId;
        this.plantStages = plantStages;
        growthIntervals = new ArrayList<>();
        for (int i = 0; i < plantStages.size() - 1; i++) {
            growthIntervals.add(growthInterval);
        }
        this.spawnCondition = spawnCondition;
        this.growthChance = growthChance;
    }

    @Override
    public String getPlantId() {
        return plantId;
    }

    @Override
    public void generatePlant(long seed, CoreChunk chunk, int x, int y, int z, Region chunkRegion) {
        if (chunk.getRegion().encompasses(x, y, z) && shouldSpawn(chunkRegion, x, y, z)) {
            BlockManager blockManager = CoreRegistry.get(BlockManager.class);
            Block lastBlock = blockManager.getBlock(plantStages.get(plantStages.size() - 1));
            chunk.setBlock(TeraMath.calcBlockPos(x, y, z), lastBlock);
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
    public Long initializePlantedPlant(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, EntityRef plant) {
        if (growthIntervals.size() > 0) {
            return growthIntervals.get(0);
        } else {
            return null;
        }
    }

    @Override
    public Long updatePlant(WorldProvider worldProvider, EnvironmentSystem environmentSystem, BlockEntityRegistry blockEntityRegistry, EntityRef plant) {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        BlockComponent block = plant.getComponent(BlockComponent.class);
        Vector3i position = block.getPosition();

        int currentIndex = plantStages.indexOf(block.getBlock().getURI());

        if (shouldGrow(plant, environmentSystem, position)) {
            int nextIndex = currentIndex + 1;
            BlockUri nextStage = plantStages.get(nextIndex);
            final boolean hasMoreStages = nextIndex < plantStages.size() - 1;

            replaceBlock(worldProvider, blockManager, plant, position, nextStage, !hasMoreStages);

            if (hasMoreStages) {
                return growthIntervals.get(nextIndex);
            } else {
                // Entered the last phase
                return null;
            }
        }
        return growthIntervals.get(currentIndex);
    }

    protected void replaceBlock(WorldProvider worldProvider, BlockManager blockManager, EntityRef plant, Vector3i position, BlockUri nextStage, boolean isLast) {
        worldProvider.setBlock(position, blockManager.getBlock(nextStage));
    }

    private boolean shouldGrow(EntityRef plant, EnvironmentSystem environmentSystem, Vector3i position) {
        float chance = 1f;
        if (growthChance != null) {
            chance = growthChance.apply(new EnvironmentLocalParameters(environmentSystem, position));
        }
        GetGrowthChance event = new GetGrowthChance(chance);
        plant.send(event);
        if (event.isConsumed()) {
            return false;
        }
        return new FastRandom().nextFloat() < TeraMath.clamp(event.getResultValue());
    }
}
