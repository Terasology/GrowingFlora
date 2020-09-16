// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.growingflora.randomUpdate;

import org.terasology.engine.entitySystem.event.Event;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public final class RandomUpdateEvent implements Event {
    private static final RandomUpdateEvent singleton = new RandomUpdateEvent();

    private RandomUpdateEvent() {
    }

    public static RandomUpdateEvent singleton() {
        return singleton;
    }
}
