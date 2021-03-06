// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gf.tree.lsystem;

import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class SurroundAxionElementGeneration implements AxionElementGeneration {
    private final TreeBlockDefinition baseBlock;
    private final TreeBlockDefinition surroundBlock;
    private final float advance;
    private final float innerRangeSquare;
    private final float range;
    private final float rangeSquare;
    private int maxZ = Integer.MAX_VALUE;

    public SurroundAxionElementGeneration(TreeBlockDefinition baseBlock, TreeBlockDefinition surroundBlock, float advance, float range) {
        this(baseBlock, surroundBlock, advance, 0, range);
    }

    public SurroundAxionElementGeneration(TreeBlockDefinition baseBlock, TreeBlockDefinition surroundBlock, float advance, float innerRange, float range) {
        this.baseBlock = baseBlock;
        this.surroundBlock = surroundBlock;
        this.advance = advance;
        this.innerRangeSquare = innerRange * innerRange;
        this.range = range;
        this.rangeSquare = range * range;
    }

    public void setMaxZ(int maxZ) {
        this.maxZ = maxZ;
    }

    @Override
    public void generate(AxionElementGenerationCallback callback, Vector3fc position, Matrix4fc rotation, String axionParameter) {
        Vector3f workVector = new Vector3f();

        callback.setMainBlock(position, baseBlock);
        int rangeInt = (int) range;
        for (int x = -rangeInt; x <= rangeInt; x++) {
            for (int y = -rangeInt; y <= rangeInt; y++) {
                for (int z = -rangeInt; z <= Math.min(rangeInt, maxZ); z++) {
                    double distanceSquare = x * x + y * y + z * z;
                    if (distanceSquare < innerRangeSquare) {
                        workVector.set(x, y, z);
                        rotation.transformDirection(workVector);
                        workVector.add(position);
                        callback.setAdditionalBlock(workVector, baseBlock);
                    } else if (distanceSquare < rangeSquare) {
                        workVector.set(x, y, z);
                        rotation.transformDirection(workVector);
                        workVector.add(position);
                        callback.setAdditionalBlock(workVector, surroundBlock);
                    }
                }
            }
        }

        callback.advance(advance);
    }
}
