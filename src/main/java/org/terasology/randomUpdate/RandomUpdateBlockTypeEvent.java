// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.randomUpdate;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.math.geom.Vector3i;

import java.util.Collection;

public class RandomUpdateBlockTypeEvent implements Event {
    private final Collection<Vector3i> blockPositions;

    public RandomUpdateBlockTypeEvent(Collection<Vector3i> blockPositions) {
        this.blockPositions = blockPositions;
    }

    public Collection<Vector3i> getBlockPositions() {
        return blockPositions;
    }
}
