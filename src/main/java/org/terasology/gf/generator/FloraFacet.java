// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gf.generator;

import org.terasology.anotherWorld.SparseObjectFacet3D;
import org.terasology.engine.math.Region3i;
import org.terasology.engine.world.generation.Border3D;

/**
 * Stores a random seed for a tree to be planted
 */
public class FloraFacet extends SparseObjectFacet3D<Float> {

    public FloraFacet(Region3i targetRegion, Border3D border) {
        super(targetRegion, border);
    }
}
