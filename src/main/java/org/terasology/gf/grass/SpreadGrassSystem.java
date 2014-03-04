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
import org.terasology.math.Vector3i;
import org.terasology.randomUpdate.RandomUpdateEvent;
import org.terasology.registry.In;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockComponent;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.entity.placement.PlaceBlocks;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class SpreadGrassSystem extends BaseComponentSystem {
    @In
    private WorldProvider worldProvider;
    @In
    private BlockManager blockManager;

    private Block dirt;
    private Block grass;

    @Override
    public void preBegin() {
        dirt = blockManager.getBlock("Core:Dirt");
        grass = blockManager.getBlock("Core:Grass");
    }

    @ReceiveEvent
    public void spreadGrass(RandomUpdateEvent randomUpdated, EntityRef entity, BlockComponent block) {
        if (block.getBlock().isGrass()) {
            Vector3i pos = block.getPosition();
            Map<Vector3i, Block> blocks = new HashMap<>();
            for (int y = -1; y <= 1; y++) {
                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        if (x != 0 || y != 0 || z != 0) {
                            Vector3i blockPosition = new Vector3i(pos.x + x, pos.y + y, pos.z + z);
                            Block blockAtPosition = worldProvider.getBlock(blockPosition);
                            if (blockAtPosition == dirt && worldProvider.getSunlight(blockPosition) > 0) {
                                blocks.put(blockPosition, grass);
                            }
                        }
                    }
                }
            }
            if (blocks.size() > 0) {
                worldProvider.getWorldEntity().send(new PlaceBlocks(blocks));
            }
        }
    }
}
