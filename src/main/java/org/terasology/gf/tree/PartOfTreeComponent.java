// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gf.tree;

import org.terasology.engine.entitySystem.Component;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class PartOfTreeComponent implements Component {
    public Part part;

    public enum Part {
        SAPLING(0),
        LEAF(1),
        BRANCH(2),
        TRUNK(3);

        private final int priority;

        Part(int priority) {
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }
    }
}
