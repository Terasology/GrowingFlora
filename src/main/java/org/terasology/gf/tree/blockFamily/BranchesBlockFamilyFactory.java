package org.terasology.gf.tree.blockFamily;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.gf.tree.PartOfTreeComponent;
import org.terasology.math.Side;
import org.terasology.math.Vector3i;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.family.ConnectionCondition;
import org.terasology.world.block.family.RegisterBlockFamilyFactory;
import org.terasology.world.block.family.UpdatesWithNeighboursFamilyFactory;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterBlockFamilyFactory("GrowingFlora:branch")
public class BranchesBlockFamilyFactory extends UpdatesWithNeighboursFamilyFactory {
    public BranchesBlockFamilyFactory() {
        super(new BranchConnectionCondition(), (byte) 63);
    }

    private static class BranchConnectionCondition implements ConnectionCondition {
        @Override
        public boolean isConnectingTo(Vector3i blockLocation, Side connectSide, WorldProvider worldProvider, BlockEntityRegistry blockEntityRegistry) {
            Vector3i neighborLocation = new Vector3i(blockLocation);
            neighborLocation.add(connectSide.getVector3i());

            EntityRef neighborEntity = blockEntityRegistry.getBlockEntityAt(neighborLocation);
            return neighborEntity != null && connectsToNeighbor(neighborEntity);
        }

        private boolean connectsToNeighbor(EntityRef neighborEntity) {
            return neighborEntity.getComponent(PartOfTreeComponent.class) != null;
        }
    }
}
