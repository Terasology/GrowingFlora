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

import org.terasology.engine.world.generation.facets.base.SparseObjectFacet3D;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Border3D;

/**
 * Stores a random seed for a tree to be planted
 */
public class FloraFacet extends SparseObjectFacet3D<Float> {

    public FloraFacet(BlockRegion targetRegion, Border3D border) {
        super(targetRegion, border);
    }
}
