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

public class TreeBlockDefinition {
    private boolean branchBlock;
    private String blockUri;

    public TreeBlockDefinition(String blockUri) {
        this(blockUri, false);
    }

    public TreeBlockDefinition(String blockUri, boolean branchBlock) {
        this.blockUri = blockUri;
        this.branchBlock = branchBlock;
    }

    public boolean isBranchBlock() {
        return branchBlock;
    }

    public String getBlockUri() {
        return blockUri;
    }
}
