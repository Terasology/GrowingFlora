// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gf.tree.lsystem;


import org.terasology.engine.world.block.ForceBlockActive;
import org.terasology.gestalt.entitysystem.component.Component;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@ForceBlockActive
public class LSystemTreeComponent implements Component<LSystemTreeComponent> {
    public String axion;
    public int generation;
    public long lastGrowthTime;
    public float branchAngle;
    public float rotationAngle;

    @Override
    public void copyFrom(LSystemTreeComponent other) {
        this.axion = other.axion;
        this.generation = other.generation;
        this.lastGrowthTime = other.lastGrowthTime;
        this.branchAngle = other.branchAngle;
        this.rotationAngle = other.rotationAngle;
    }
}
