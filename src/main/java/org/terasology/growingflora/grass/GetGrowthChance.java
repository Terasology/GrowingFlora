// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.growingflora.grass;

import org.terasology.engine.entitySystem.event.AbstractConsumableValueModifiableEvent;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class GetGrowthChance extends AbstractConsumableValueModifiableEvent {
    public GetGrowthChance(float baseChance) {
        super(baseChance);
    }
}
