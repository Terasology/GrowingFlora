// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gf.tree.blockFamily;

import org.joml.Vector3ic;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.math.Rotation;
import org.terasology.engine.math.Side;
import org.terasology.engine.math.SideBitFlag;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockBuilderHelper;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.engine.world.block.BlockUri;
import org.terasology.engine.world.block.family.BlockSections;
import org.terasology.engine.world.block.family.MultiConnectFamily;
import org.terasology.engine.world.block.family.RegisterBlockFamily;
import org.terasology.engine.world.block.loader.BlockFamilyDefinition;
import org.terasology.engine.world.block.shapes.BlockShape;
import org.terasology.gestalt.naming.Name;

@RegisterBlockFamily("GrowingFlora:branch")
@BlockSections({"no_connections", "one_connection", "line_connection", "2d_corner", "3d_corner", "2d_t", "cross", "3d_side", "five_connections", "all"})
public class BranchesBlockFamily extends MultiConnectFamily {

    public BranchesBlockFamily(BlockFamilyDefinition definition, BlockShape shape, BlockBuilderHelper blockBuilder) {
        super(definition, shape, blockBuilder);
    }

    public BranchesBlockFamily(BlockFamilyDefinition definition, BlockBuilderHelper blockBuilder) {
        super(definition, blockBuilder);

        BlockUri blockUri = new BlockUri(definition.getUrn());
        Block block = blockBuilder.constructSimpleBlock(definition, blockUri, this);

        block.setBlockFamily(this);
        block.setUri(new BlockUri(blockUri, new Name(String.valueOf(0))));
        this.blocks.put((byte) 0, block);

        this.registerBlock(blockUri, definition, blockBuilder, "no_connections", (byte) 0, Rotation.allValues());
        this.registerBlock(blockUri, definition, blockBuilder, "one_connection", SideBitFlag.getSides(Side.BACK), Rotation.allValues());
        this.registerBlock(blockUri, definition, blockBuilder, "line_connection", SideBitFlag.getSides(Side.BACK, Side.FRONT), Rotation.allValues());
        this.registerBlock(blockUri, definition, blockBuilder, "2d_corner", SideBitFlag.getSides(Side.LEFT, Side.BACK), Rotation.allValues());
        this.registerBlock(blockUri, definition, blockBuilder, "3d_corner", SideBitFlag.getSides(Side.LEFT, Side.BACK, Side.TOP), Rotation.allValues());
        this.registerBlock(blockUri, definition, blockBuilder, "2d_t", SideBitFlag.getSides(Side.LEFT, Side.BACK, Side.FRONT), Rotation.allValues());
        this.registerBlock(blockUri, definition, blockBuilder, "cross", SideBitFlag.getSides(Side.RIGHT, Side.LEFT, Side.BACK, Side.FRONT), Rotation.allValues());
        this.registerBlock(blockUri, definition, blockBuilder, "3d_side", SideBitFlag.getSides(Side.LEFT, Side.BACK, Side.FRONT, Side.TOP), Rotation.allValues());
        this.registerBlock(blockUri, definition, blockBuilder, "five_connections", SideBitFlag.getSides(Side.LEFT, Side.BACK, Side.FRONT, Side.TOP, Side.BOTTOM), Rotation.allValues());
        this.registerBlock(blockUri, definition, blockBuilder, "all", (byte) 63, Rotation.allValues());
    }

    @Override
    public byte getConnectionSides() {
        return (byte) 63;
    }

    @Override
    public Block getArchetypeBlock() {
        return blocks.get((byte) 0);
    }

    @Override
    protected boolean connectionCondition(Vector3ic blockLocation, Side connectSide) {

        org.joml.Vector3i neighborLocation = new org.joml.Vector3i(blockLocation);
        neighborLocation.add(connectSide.direction());

        EntityRef neighborEntity = blockEntityRegistry.getEntityAt(neighborLocation);
        BlockComponent blockComponent = neighborEntity.getComponent(BlockComponent.class);

        return neighborEntity.hasComponent(ConnectsToBranchesComponent.class)
            || (blockComponent != null && blockComponent.getBlock().isFullSide(connectSide));
    }
}
