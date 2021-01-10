// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gf.tree.lsystem;

import org.joml.Matrix4fc;
import org.joml.Vector3fc;

public interface AxionElementGeneration {
    void generate(AxionElementGenerationCallback callback, Vector3fc position, Matrix4fc rotation, String axionParameter);

    public interface AxionElementGenerationCallback {
        void setMainBlock(Vector3fc position, TreeBlockDefinition blockDefinition);

        void setAdditionalBlock(Vector3fc position, TreeBlockDefinition blockDefinition);

        void advance(float distance);
    }
}
