// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gf.tree.lsystem;

import org.joml.Vector3f;
import org.terasology.math.geom.Matrix4f;

public class DefaultAxionElementGeneration implements AxionElementGeneration {
    private TreeBlockDefinition block;
    private float advance;

    public DefaultAxionElementGeneration(TreeBlockDefinition block, float advance) {
        this.block = block;
        this.advance = advance;
    }

    @Override
    public void generate(AxionElementGenerationCallback callback, Vector3f position, Matrix4f rotation, String axionParameter) {
        callback.setMainBlock(position, block);
        callback.advance(advance);
    }
}
