// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.growingflora.grass;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.entity.placement.PlaceBlocks;
import org.terasology.engine.world.block.typeEntity.BlockTypeComponent;
import org.terasology.growingflora.randomUpdate.RandomUpdateBlockTypeEvent;
import org.terasology.math.geom.Vector3i;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
public class SpreadGrassSystem extends BaseComponentSystem {
    private final int[][] positions = new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
    @In
    private WorldProvider worldProvider;
    @In
    private BlockManager blockManager;
    private Block dirt;
    private Block grass;

    @Override
    public void preBegin() {
        dirt = blockManager.getBlock("CoreAssets:Dirt");
        grass = blockManager.getBlock("CoreAssets:Grass");
    }

    @ReceiveEvent
    public void spreadGrass(RandomUpdateBlockTypeEvent randomUpdated, EntityRef entity, BlockTypeComponent block) {
        if (block.block.isGrass()) {
            for (Vector3i pos : randomUpdated.getBlockPositions()) {
                checkForGrassSpreadAround(pos);
            }
        }
    }

    private void checkForGrassSpreadAround(Vector3i pos) {
        Map<Vector3i, Block> blocks = new HashMap<>();
        for (int[] position : positions) {
            for (int y = 1; y >= -1; y--) {
                Vector3i blockPosition = new Vector3i(pos.x + position[0], pos.y + y, pos.z + position[1]);
                if (worldProvider.isBlockRelevant(blockPosition)) {
                    Block blockAtPosition = worldProvider.getBlock(blockPosition);
                    if (blockAtPosition.isPenetrable()) {
                        if (blockAtPosition == dirt && blockAboveIsLoadedAndAir(blockPosition)) {
                            blocks.put(blockPosition, grass);
                            break;
                        }
                    }
                }
            }
        }

        if (blocks.size() > 0) {
            worldProvider.getWorldEntity().send(new PlaceBlocks(blocks));
        }
    }

    private boolean blockAboveIsLoadedAndAir(Vector3i blockPosition) {
        Vector3i blockAbove = new Vector3i(blockPosition.x, blockPosition.y + 1, blockPosition.z);
        return worldProvider.isBlockRelevant(blockAbove)
                && worldProvider.getBlock(blockAbove).isPenetrable();
    }
}
