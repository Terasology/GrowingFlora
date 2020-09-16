// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.growingflora.tree.lsystem;

import org.terasology.math.geom.Matrix4f;
import org.terasology.math.geom.Vector3f;


/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface AxionElementGeneration {
    void generate(AxionElementGenerationCallback callback, Vector3f position, Matrix4f rotation, String axionParameter);

    interface AxionElementGenerationCallback {
        void setMainBlock(Vector3f position, TreeBlockDefinition blockDefinition);

        void setAdditionalBlock(Vector3f position, TreeBlockDefinition blockDefinition);

        void advance(float distance);
    }
}
