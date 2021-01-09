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
import org.joml.Vector3ic;
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.math.JomlUtil;
import org.terasology.math.geom.Vector3i;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.utilities.random.FastRandom;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.Chunks;
import org.terasology.world.chunks.event.BeforeChunkUnload;
import org.terasology.world.chunks.event.OnChunkLoaded;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
public class RandomUpdateSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    private Set<Vector3ic> loadedChunks = new HashSet<>();

    private long lastUpdate;
    private int updateInterval = 20;

    private int updateCountPerChunk = 3;

    @In
    private Time time;
    @In
    private BlockEntityRegistry blockEntityRegistry;
    @In
    private WorldProvider worldProvider;

    @In
    private BlockManager blockManager;

    @ReceiveEvent
    public void addChunk(OnChunkLoaded chunkLoaded, EntityRef worldEntity) {
        loadedChunks.add(new org.joml.Vector3i(chunkLoaded.getChunkPos()));
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

            PerformanceMonitor.startActivity("Random Update System");
            try {
                FastRandom rand = new FastRandom();
                Multimap<Block, Vector3i> nonEntityUpdates = HashMultimap.create();

                for (Vector3ic loadedChunk : loadedChunks) {
                    for (int i = 0; i < updateCountPerChunk; i++) {
                        //TODO: reduce the number of vector allocations here
                        //      - only compute the chunk location once: loadedChunk * Chunks.SIZE for each loaded chunk
                        //      - reuse the allocated vector for different chunks
                        //      - compute the random position in another temporary vector
                        //      - only create new instance when storing the block in `nonEntityUpdates`
                        final org.joml.Vector3i blockPosition = new org.joml.Vector3i(
                                loadedChunk.x() * Chunks.SIZE_X + rand.nextInt(Chunks.SIZE_X),
                                loadedChunk.y() * Chunks.SIZE_Y + rand.nextInt(Chunks.SIZE_Y),
                                loadedChunk.z() * Chunks.SIZE_Z + rand.nextInt(Chunks.SIZE_Z));
                        EntityRef entity = blockEntityRegistry.getExistingBlockEntityAt(blockPosition);
                        if (entity.exists()) {
                            entity.send(RandomUpdateEvent.singleton());
                        } else {
                            final Block block = worldProvider.getBlock(blockPosition);
                            if (block.isPenetrable()) {
                                nonEntityUpdates.put(block, JomlUtil.from(blockPosition));
                            }
                        }
                    }
                }

                for (Block block : nonEntityUpdates.keySet()) {
                    block.getEntity().send(new RandomUpdateBlockTypeEvent(nonEntityUpdates.get(block)));
                }
            } finally {
                PerformanceMonitor.endActivity();
            }
        }
    }
}
