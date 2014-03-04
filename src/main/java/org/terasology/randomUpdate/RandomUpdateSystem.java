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
package org.terasology.randomUpdate;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.math.Vector3i;
import org.terasology.registry.In;
import org.terasology.utilities.random.FastRandom;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.event.BeforeChunkUnload;
import org.terasology.world.chunks.event.OnChunkLoaded;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
//@RegisterSystem
public class RandomUpdateSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    private Set<Vector3i> loadedChunks = new HashSet<>();

    private long lastUpdate;
    private int updateInterval = 100;

    private int updateCountPerChunk = 3;

    @In
    private Time time;
    @In
    private BlockEntityRegistry blockEntityRegistry;
    @In
    private WorldProvider worldProvider;

    @ReceiveEvent
    public void addChunk(OnChunkLoaded chunkLoaded, EntityRef worldEntity) {
        loadedChunks.add(chunkLoaded.getChunkPos());
    }

    @ReceiveEvent
    public void removeChunk(BeforeChunkUnload chunkUnload, EntityRef worldEntity) {
        loadedChunks.remove(chunkUnload.getChunkPos());
    }

    @Override
    public void update(float delta) {
        long currentTime = time.getGameTimeInMs();
        if (lastUpdate + updateInterval <= currentTime) {
            lastUpdate = currentTime;

            FastRandom rand = new FastRandom();
            Multimap<Block, Vector3i> nonEntityUpdates = HashMultimap.create();

            for (Vector3i loadedChunk : loadedChunks) {
                for (int i = 0; i < updateCountPerChunk; i++) {
                    int chunkX = rand.nextInt(ChunkConstants.SIZE_X);
                    int chunkY = rand.nextInt(ChunkConstants.SIZE_Y);
                    int chunkZ = rand.nextInt(ChunkConstants.SIZE_Z);

                    final Vector3i blockPosition = new Vector3i(
                            loadedChunk.x * ChunkConstants.SIZE_X + chunkX,
                            loadedChunk.y * ChunkConstants.SIZE_Y + chunkY,
                            loadedChunk.z * ChunkConstants.SIZE_Z + chunkZ);
                    EntityRef entity = blockEntityRegistry.getExistingBlockEntityAt(blockPosition);
                    if (entity.exists()) {
                        entity.send(RandomUpdateEvent.singleton());
                    } else {
                        final Block block = worldProvider.getBlock(blockPosition);
                        if (block != BlockManager.getAir()) {
                            nonEntityUpdates.put(block, blockPosition);
                        }
                    }
                }
            }

            for (Block block : nonEntityUpdates.keySet()) {
                block.getEntity().send(new RandomUpdateBlockTypeEvent(nonEntityUpdates.get(block)));
            }
        }
    }
}
