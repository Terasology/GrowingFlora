// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gf.tree.lsystem;

import org.joml.Vector3f;
import org.terasology.math.geom.Matrix4f;


/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class AdvanceAxionElementGeneration implements AxionElementGeneration {
    private float advance;

    public AdvanceAxionElementGeneration(float advance) {
        this.advance = advance;
    }

    @Override
    public void generate(AxionElementGenerationCallback callback, Vector3f position, Matrix4f rotation, String axionParameter) {
        callback.advance(advance);
    }
}
