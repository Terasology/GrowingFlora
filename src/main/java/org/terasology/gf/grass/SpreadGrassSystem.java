// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gf.grass;

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.entity.placement.PlaceBlocks;
import org.terasology.engine.world.block.typeEntity.BlockTypeComponent;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.randomUpdate.RandomUpdateBlockTypeEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
public class SpreadGrassSystem extends BaseComponentSystem {
    @In
    private WorldProvider worldProvider;
    @In
    private BlockManager blockManager;

    private Block dirt;
    private Block grass;

    private int[][] positions = new int[][]{{-1, 0}, {1, 0}, {0, -1}, {0, 1}};

    @Override
    public void preBegin() {
        dirt = blockManager.getBlock("CoreAssets:Dirt");
        grass = blockManager.getBlock("CoreAssets:Grass");
    }

    @ReceiveEvent
    public void spreadGrass(RandomUpdateBlockTypeEvent randomUpdated, EntityRef entity, BlockTypeComponent block) {
        if (block.block.isGrass()) {
            for (Vector3ic pos : randomUpdated.getBlockPositions()) {
                checkForGrassSpreadAround(pos);
            }
        }
    }

    private void checkForGrassSpreadAround(Vector3ic pos) {
        Map<org.joml.Vector3i, Block> blocks = new HashMap<>();
        for (int[] position : positions) {
            for (int y = 1; y >= -1; y--) {
                Vector3i blockPosition = new Vector3i(pos.x() + position[0], pos.y() + y, pos.z() + position[1]);
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
