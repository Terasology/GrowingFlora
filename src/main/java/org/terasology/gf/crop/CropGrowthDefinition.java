package org.terasology.gf.crop;

import org.terasology.anotherWorld.GenerationParameters;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.gf.generator.PlantGrowthDefinition;
import org.terasology.math.Vector3i;
import org.terasology.registry.CoreRegistry;
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

    public CropGrowthDefinition(String plantId, List<BlockUri> plantStages, long growthInterval) {
        this.plantId = plantId;
        this.plantStages = plantStages;
        this.growthInterval = growthInterval;
    }

    @Override
    public String getPlantId() {
        return plantId;
    }

    @Override
    public void generatePlant(String seed, Vector3i chunkPos, ChunkView chunkView, int x, int y, int z, GenerationParameters generationParameters) {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        Block lastBlock = blockManager.getBlock(plantStages.get(plantStages.size() - 1));
        chunkView.setBlock(x, y, z, lastBlock);
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
}
