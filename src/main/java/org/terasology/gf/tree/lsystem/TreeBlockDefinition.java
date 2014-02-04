/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.gf.tree.lsystem;

import org.terasology.gf.tree.PartOfTreeComponent;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.BlockManager;

public class TreeBlockDefinition {
    private String blockUri;
    private PartOfTreeComponent.Part treePart;

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
