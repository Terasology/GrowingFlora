// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.growingflora.tree.lsystem;

import org.terasology.math.geom.Matrix4f;
import org.terasology.math.geom.Vector3f;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class DefaultAxionElementGeneration implements AxionElementGeneration {
    private final TreeBlockDefinition block;
    private final float advance;

    public DefaultAxionElementGeneration(TreeBlockDefinition block, float advance) {
        this.block = block;
        this.advance = advance;
    }

    @Override
    public void generate(AxionElementGenerationCallback callback, Vector3f position, Matrix4f rotation,
                         String axionParameter) {
        callback.setMainBlock(position, block);
        callback.advance(advance);
    }
}
