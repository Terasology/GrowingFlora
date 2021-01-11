// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gf.generator;

import org.joml.Vector3i;
import org.terasology.entitySystem.entity.EntityRef;
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
