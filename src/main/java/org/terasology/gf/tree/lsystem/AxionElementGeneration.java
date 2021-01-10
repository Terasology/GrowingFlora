// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gf.tree.lsystem;

import org.joml.Vector3f;
import org.terasology.math.geom.Matrix4f;


/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface AxionElementGeneration {
    void generate(AxionElementGenerationCallback callback, Vector3f position, Matrix4f rotation, String axionParameter);

    public interface AxionElementGenerationCallback {
        void setMainBlock(Vector3f position, TreeBlockDefinition blockDefinition);

        void setAdditionalBlock(Vector3f position, TreeBlockDefinition blockDefinition);

        void advance(float distance);
    }
}
