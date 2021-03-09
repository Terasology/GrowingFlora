// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.randomUpdate;

import org.joml.Vector3i;
import org.terasology.engine.entitySystem.event.Event;

import java.util.Collection;

public class RandomUpdateBlockTypeEvent implements Event {
    private Collection<Vector3i> blockPositions;

    public RandomUpdateBlockTypeEvent(Collection<Vector3i> blockPositions) {
        this.blockPositions = blockPositions;
    }

    public Collection<Vector3i> getBlockPositions() {
        return blockPositions;
    }
}
