// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gf.tree.lsystem;


import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.world.block.ForceBlockActive;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@ForceBlockActive
public class LSystemTreeComponent implements Component {
    public String axion;
    public int generation;
    public long lastGrowthTime;
    public float branchAngle;
    public float rotationAngle;
}
