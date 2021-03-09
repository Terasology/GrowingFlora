// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gf.tree;

import org.joml.Vector3f;
import org.joml.Vector3i;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.prefab.PrefabManager;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.health.BeforeDestroyEvent;
import org.terasology.engine.logic.health.DestroyEvent;
import org.terasology.engine.logic.inventory.InventoryComponent;
import org.terasology.engine.logic.inventory.InventoryManager;
import org.terasology.engine.logic.inventory.events.DropItemEvent;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.engine.world.block.entity.neighbourUpdate.LargeBlockUpdateFinished;
import org.terasology.engine.world.block.entity.neighbourUpdate.LargeBlockUpdateStarting;
import org.terasology.gf.LivingPlantComponent;
import org.terasology.gf.PlantRegistry;
import org.terasology.gf.generator.ConnectedPlantGrowthDefinition;
import org.terasology.gf.generator.PlantGrowthDefinition;
import org.terasology.gf.tree.lsystem.LSystemTreeComponent;

import java.util.Collection;

@RegisterSystem(RegisterMode.AUTHORITY)
public class TreeDestructionSystem extends BaseComponentSystem {
    @In
    private EntityManager entityManager;
    @In
    private PlantRegistry plantRegistry;
    @In
    private WorldProvider worldProvider;
    @In
    private BlockEntityRegistry blockEntityRegistry;
    @In
    private InventoryManager inventoryManager;
    @In
    private PrefabManager prefabManager;

    private boolean processingDestruction;

    // There is some bug in Engine, which makes it impossible to do that
    @ReceiveEvent
    public void onTreePartDestroyed(BeforeDestroyEvent beforeDestroyEvent, EntityRef treePart,
                                    PartOfTreeComponent partOfTreeComponent, BlockComponent component) {
        if (!processingDestruction) {
            processingDestruction = true;
            try {
                Vector3i position = component.getPosition(new Vector3i());

                for (EntityRef testedTree : entityManager.getEntitiesWith(LSystemTreeComponent.class)) {
                    BlockComponent blockComponent = testedTree.getComponent(BlockComponent.class);
                    if (blockComponent != null) {
                        Vector3i testedPosition = blockComponent.getPosition(new Vector3i());

                        double distance = Math.sqrt((testedPosition.x - position.x) * (testedPosition.x - position.x)
                                + (testedPosition.z - position.z) * (testedPosition.z - position.z));
                        if (distance < Math.sqrt(512)) {
                            String type = testedTree.getComponent(LivingPlantComponent.class).type;
                            PlantGrowthDefinition plantGrowthDefinition = plantRegistry.getPlantGrowthDefinition(type);
                            if (plantGrowthDefinition instanceof ConnectedPlantGrowthDefinition) {
                                ConnectedPlantGrowthDefinition plantDef = (ConnectedPlantGrowthDefinition) plantGrowthDefinition;
                                Collection<Vector3i> blocksConnectedTo = plantDef.getBlocksConnectedTo(worldProvider, blockEntityRegistry, position, testedTree);
                                if (blocksConnectedTo != null) {
                                    destroyTheConnectedBlocksAndGatherItems(position, blocksConnectedTo);
                                }
                            }
                        }
                    }
                }
            } finally {
                processingDestruction = false;
            }
        }
    }

    private void destroyTheConnectedBlocksAndGatherItems(Vector3i position, Collection<Vector3i> blocksConnectedTo) {
        EntityRef worldEntity = worldProvider.getWorldEntity();
        worldEntity.send(new LargeBlockUpdateStarting());
        try {
            EntityRef tempInventoryEntity = entityManager.create();
            try {
                InventoryComponent inventory = new InventoryComponent(20);
                tempInventoryEntity.addComponent(inventory);
                Prefab damagePrefab = prefabManager.getPrefab("GrowingFlora:TreeCutDamage");

                for (Vector3i vector3i : blocksConnectedTo) {
                    blockEntityRegistry.getEntityAt(vector3i).send(
                            new DestroyEvent(tempInventoryEntity, EntityRef.NULL, damagePrefab));
                }

                for (int i = 0; i < 20; i++) {
                    EntityRef item = inventoryManager.getItemInSlot(tempInventoryEntity, i);
                    if (item.exists()) {
                        item.send(new DropItemEvent(new Vector3f(position)));
                    }
                }
            } finally {
                tempInventoryEntity.destroy();
            }
        } finally {
            worldEntity.send(new LargeBlockUpdateFinished());
        }
    }
}
