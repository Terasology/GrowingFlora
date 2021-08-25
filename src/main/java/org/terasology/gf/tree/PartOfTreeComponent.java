// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gf.tree;

import org.terasology.gestalt.entitysystem.component.Component;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public class PartOfTreeComponent implements Component<PartOfTreeComponent> {
    @Override
    public void copyFrom(PartOfTreeComponent other) {
        this.part = other.part;
    }

    public enum Part {
        SAPLING(0),
        LEAF(1),
        BRANCH(2),
        TRUNK(3);

        private int priority;

        private Part(int priority) {
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }
    }

    public Part part;
}
