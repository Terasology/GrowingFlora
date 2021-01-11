// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gf.tree.lsystem;

import org.joml.Matrix4fc;
import org.joml.Vector3fc;

public class AdvanceAxionElementGeneration implements AxionElementGeneration {
    private float advance;

    public AdvanceAxionElementGeneration(float advance) {
        this.advance = advance;
    }

    @Override
    public void generate(AxionElementGenerationCallback callback, Vector3fc position, Matrix4fc rotation, String axionParameter) {
        callback.advance(advance);
    }
}
