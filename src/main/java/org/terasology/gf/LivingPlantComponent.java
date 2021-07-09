// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gf;

import org.terasology.gestalt.entitysystem.component.Component;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class LivingPlantComponent implements Component<LivingPlantComponent> {
    public String type;

    @Override
    public void copy(LivingPlantComponent other) {
        this.type = other.type;
    }
}
