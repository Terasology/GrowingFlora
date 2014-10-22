/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.gf.grass;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.math.Vector3i;
import org.terasology.randomUpdate.RandomUpdateBlockTypeEvent;
import org.terasology.registry.In;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.entity.placement.PlaceBlocks;
import org.terasology.world.block.typeEntity.BlockTypeComponent;

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
        dirt = blockManager.getBlock("Core:Dirt");
        grass = blockManager.getBlock("Core:Grass");
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
                    if (blockAtPosition != BlockManager.getAir()) {
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
        return worldProvider.isBlockRelevant(blockAbove) &&
            worldProvider.getBlock(blockAbove) == BlockManager.getAir();
    }
}
