// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.randomUpdate;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.engine.monitoring.PerformanceMonitor;
import org.terasology.engine.registry.In;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.chunks.event.BeforeChunkUnload;
import org.terasology.engine.world.chunks.event.OnChunkLoaded;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;

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
                                nonEntityUpdates.put(block, blockPosition);
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
