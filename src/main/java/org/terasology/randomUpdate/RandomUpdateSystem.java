// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.randomUpdate;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.terasology.engine.core.Time;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
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
import org.terasology.engine.world.chunks.ChunkConstants;
import org.terasology.engine.world.chunks.event.BeforeChunkUnload;
import org.terasology.engine.world.chunks.event.OnChunkLoaded;
import org.terasology.math.geom.Vector3i;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
public class RandomUpdateSystem extends BaseComponentSystem implements UpdateSubscriberSystem {
    private final Set<Vector3i> loadedChunks = new HashSet<>();
    private final int updateInterval = 20;
    private final int updateCountPerChunk = 3;
    private long lastUpdate;
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

            PerformanceMonitor.startActivity("Random Update System");
            try {
                FastRandom rand = new FastRandom();
                Multimap<Block, Vector3i> nonEntityUpdates = HashMultimap.create();

                for (Vector3i loadedChunk : loadedChunks) {
                    for (int i = 0; i < updateCountPerChunk; i++) {
                        final Vector3i blockPosition = new Vector3i(
                                loadedChunk.x * ChunkConstants.SIZE_X + rand.nextInt(ChunkConstants.SIZE_X),
                                loadedChunk.y * ChunkConstants.SIZE_Y + rand.nextInt(ChunkConstants.SIZE_Y),
                                loadedChunk.z * ChunkConstants.SIZE_Z + rand.nextInt(ChunkConstants.SIZE_Z));
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
