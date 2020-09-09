// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gf.tree.lsystem;

import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.gf.tree.PartOfTreeComponent;

public class TreeBlockDefinition {
    private final String blockUri;
    private final PartOfTreeComponent.Part treePart;

    public TreeBlockDefinition(String blockUri, PartOfTreeComponent.Part treePart) {
        this.blockUri = blockUri;
        this.treePart = treePart;

        // Preload block
        CoreRegistry.get(BlockManager.class).getBlockFamily(blockUri);
    }

    public boolean isBranchBlock() {
        return treePart == PartOfTreeComponent.Part.BRANCH;
    }

    public String getBlockUri() {
        return blockUri;
    }

    public PartOfTreeComponent.Part getTreePart() {
        return treePart;
    }
}
