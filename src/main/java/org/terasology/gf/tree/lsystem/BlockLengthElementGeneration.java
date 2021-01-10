// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gf.tree.lsystem;

import org.joml.Vector3f;
import org.terasology.math.geom.Matrix4f;


public class BlockLengthElementGeneration implements AxionElementGeneration {
    private TreeBlockDefinition block;
    private float advance;

    public BlockLengthElementGeneration(TreeBlockDefinition block, float advance) {
        this.block = block;
        this.advance = advance;
    }

    @Override
    public void generate(AxionElementGenerationCallback callback, Vector3f position, Matrix4f rotation, String axionParameter) {
        float length = Float.parseFloat(axionParameter);

        for (float pos = advance; pos < length; pos += advance) {
            callback.setMainBlock(position, block);
            callback.advance(advance);
        }
    }
}
