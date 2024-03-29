// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gf.generator;

import org.terasology.engine.world.generation.facets.base.SparseObjectFacet3D;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Border3D;

/**
 * Stores a random seed for a tree to be planted
 */
public class TreeFacet extends SparseObjectFacet3D<Float> {

    public TreeFacet(BlockRegion targetRegion, Border3D border) {
        super(targetRegion, border);
    }
}
