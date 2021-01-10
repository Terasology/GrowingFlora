// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gf.tree.lsystem;

import org.joml.Matrix4fc;
import org.joml.Vector3fc;

public class DefaultAxionElementGeneration implements AxionElementGeneration {
    private TreeBlockDefinition block;
    private float advance;

    public DefaultAxionElementGeneration(TreeBlockDefinition block, float advance) {
        this.block = block;
        this.advance = advance;
    }

    @Override
    public void generate(AxionElementGenerationCallback callback, Vector3fc position, Matrix4fc rotation, String axionParameter) {
        callback.setMainBlock(position, block);
        callback.advance(advance);
    }
}
