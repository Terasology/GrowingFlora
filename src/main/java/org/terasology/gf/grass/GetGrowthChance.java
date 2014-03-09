/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.gf.grass;

import gnu.trove.iterator.TFloatIterator;
import gnu.trove.list.TFloatList;
import gnu.trove.list.array.TFloatArrayList;
import org.terasology.entitySystem.event.AbstractConsumableEvent;
import org.terasology.math.TeraMath;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class GetGrowthChance extends AbstractConsumableEvent {
    private float baseChance;

    private TFloatList multipliers = new TFloatArrayList();
    private TFloatList modifiers = new TFloatArrayList();

    public GetGrowthChance(float baseChance) {
        this.baseChance = baseChance;
    }

    public float getBaseChance() {
        return baseChance;
    }

    public void multiply(float amount) {
        this.multipliers.add(amount);
    }

    public void add(float amount) {
        modifiers.add(amount);
    }

    public void subtract(float amount) {
        modifiers.add(-amount);
    }

    public float calculateTotal() {
        // For now, add all modifiers and multiply by all multipliers. Negative modifiers cap to zero, but negative
        // multipliers remain (so damage can be flipped to healing)

        float total = baseChance;
        TFloatIterator modifierIter = modifiers.iterator();
        while (modifierIter.hasNext()) {
            total += modifierIter.next();
        }
        total = Math.max(0, total);
        if (total == 0) {
            return 0;
        }
        TFloatIterator multiplierIter = multipliers.iterator();
        while (multiplierIter.hasNext()) {
            total *= multiplierIter.next();
        }
        return (float) TeraMath.clamp(total);
    }
}
