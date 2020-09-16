// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.growingflora.tree.lsystem;

import org.terasology.engine.utilities.random.Random;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface AxionElementReplacement {
    String getReplacement(Random random, String parameter, String currentAxion);
}
