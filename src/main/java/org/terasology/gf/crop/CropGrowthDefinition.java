package org.terasology.gf.crop;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import org.terasology.anotherWorld.GenerationLocalParameters;
import org.terasology.anotherWorld.GenerationParameters;
import org.terasology.anotherWorld.LocalParameters;
import org.terasology.anotherWorld.WorldLocalParameters;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.gf.generator.PlantGrowthDefinition;
import org.terasology.math.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.utilities.random.FastRandom;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.ChunkView;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;

import java.util.List;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class CropGrowthDefinition implements PlantGrowthDefinition {
    private String plantId;
    private List<BlockUri> plantStages;
    private long growthInterval;
    private Predicate<LocalParameters> spawnCondition;
    private Function<LocalParameters, Float> growthChance;

    public CropGrowthDefinition(String plantId, List<BlockUri> plantStages, long growthInterval,
                                Predicate<LocalParameters> spawnCondition, Function<LocalParameters, Float> growthChance) {
        this.plantId = plantId;
        this.plantStages = plantStages;
        this.growthInterval = growthInterval;
        this.spawnCondition = spawnCondition;
        this.growthChance = growthChance;
    }

    @Override
    public String getPlantId() {
        return plantId;
    }

    @Override
    public void generatePlant(String seed, Vector3i chunkPos, ChunkView chunkView, int x, int y, int z, GenerationParameters generationParameters) {
        if (shouldSpawn(generationParameters, x, y, z)) {
            BlockManager blockManager = CoreRegistry.get(BlockManager.class);
            Block lastBlock = blockManager.getBlock(plantStages.get(plantStages.size() - 1));
            chunkView.setBlock(x, y, z, lastBlock);
        }
    }

    private boolean shouldSpawn(GenerationParameters generationParameters, int x, int y, int z) {
        return spawnCondition == null || spawnCondition.apply(new GenerationLocalParameters(generationParameters, new Vector3i(x, y, z)));
    }

    @Override
    public Long initializeGeneratedPlant(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, EntityRef plant) {
        return null;
    }

    @Override
    public Long initializePlantedPlant(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, EntityRef plant) {
        return growthInterval;
    }

    @Override
    public Long updatePlant(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, EntityRef plant) {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        BlockComponent block = plant.getComponent(BlockComponent.class);
        Vector3i position = block.getPosition();

        if (shouldGrow(worldProvider, position)) {
            int previousIndex = plantStages.indexOf(block.getBlock().getURI());
            int nextIndex = previousIndex + 1;
            BlockUri nextStage = plantStages.get(nextIndex);
            worldProvider.setBlock(position, blockManager.getBlock(nextStage));

            if (nextIndex < plantStages.size() - 1) {
                return growthInterval;
            } else {
                // Entered the last phase
                return null;
            }
        }
        return growthInterval;
    }

    private boolean shouldGrow(WorldProvider worldProvider, Vector3i position) {
        if (growthChance == null) {
            return true;
        }

        float growthChance = this.growthChance.apply(new WorldLocalParameters(worldProvider, position));
        FastRandom rnd = new FastRandom();
        return rnd.nextFloat() < growthChance;
    }
}
