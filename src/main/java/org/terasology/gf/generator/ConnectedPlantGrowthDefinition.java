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
package org.terasology.gf.generator;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;

import java.util.Collection;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface ConnectedPlantGrowthDefinition extends PlantGrowthDefinition {
    /**
     * Checks whether specified block in the world belongs to this plant.
     *
     * @param worldProvider
     * @param blockEntityRegistry
     * @param block
     * @param plant
     * @return
     */
    boolean isBlockOwnedByPlant(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, Vector3i block, EntityRef plant);

    /**
     * Returns a collection of locations that are dependand on existance of this block in this plant.
     * If this plant doesn't own this block, <code>null</code> will be returned to indicate this.
     *
     * @param worldProvider
     * @param blockEntityRegistry
     * @param block
     * @param plant
     * @return
     */
    Collection<Vector3i> getBlocksConnectedTo(WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry, Vector3i block, EntityRef plant);
}
