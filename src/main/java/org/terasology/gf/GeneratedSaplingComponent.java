// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gf;

import org.terasology.engine.world.block.ForceBlockActive;
import org.terasology.gestalt.entitysystem.component.Component;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@ForceBlockActive
public class GeneratedSaplingComponent implements Component<GeneratedSaplingComponent> {
    public String type;

    @Override
    public void copyFrom(GeneratedSaplingComponent other) {
        this.type = other.type;
    }
}
