/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.gf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.OnAddedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.gf.generator.PlantGrowthDefinition;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.math.Vector3i;
import org.terasology.monitoring.PerformanceMonitor;
import org.terasology.registry.In;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockComponent;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class SaplingInitializeSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(SaplingInitializeSystem.class);
    public static final String INITIALIZE_PLANT_ACTION = "PlantPack:initializePlant";

    @In
    private PlantRegistry plantRegistry;
    @In
    private WorldProvider worldProvider;
    @In
    private BlockEntityRegistry blockEntityRegistry;
    @In
    private DelayManager delayManager;

    // To avoid stack overflow
    private boolean processingEvent;

    @ReceiveEvent(components = {GeneratedSaplingComponent.class, BlockComponent.class})
    public void generatedSaplingLoaded(OnAddedComponent event, EntityRef sapling) {
        delayManager.addDelayedAction(sapling, INITIALIZE_PLANT_ACTION, 0);
    }

    @ReceiveEvent(components = {PlantedSaplingComponent.class, LivingPlantComponent.class, BlockComponent.class})
    public void plantedSapling(OnAddedComponent event, EntityRef sapling, LivingPlantComponent livingPlant) {
        if (!processingEvent) {
            processingEvent = true;
            try {
                Vector3i blockLocation = sapling.getComponent(BlockComponent.class).getPosition();
                String saplingType = livingPlant.type;
                PlantGrowthDefinition plantDefinition = plantRegistry.getPlantGrowthDefinition(saplingType);
                Long updateDelay = plantDefinition.initializePlantedPlant(worldProvider, blockEntityRegistry, sapling);
                EntityRef blockEntity = blockEntityRegistry.getBlockEntityAt(blockLocation);
                if (blockEntity.hasComponent(PlantedSaplingComponent.class)) {
                    blockEntity.removeComponent(PlantedSaplingComponent.class);
                }
                if (updateDelay != null) {
                    delayManager.addDelayedAction(blockEntity, PlantGrowingSystem.UPDATE_PLANT_ACTION_ID, updateDelay);
                }
            } finally {
                processingEvent = false;
            }
        }
    }

    @ReceiveEvent(components = {GeneratedSaplingComponent.class, BlockComponent.class})
    public void delayedInitialization(DelayedActionTriggeredEvent event, EntityRef sapling) {
        if (event.getActionId().equals(INITIALIZE_PLANT_ACTION)) {
            if (!processingEvent) {
                PerformanceMonitor.startActivity("GrowingFlora - Initializing sapling");
                processingEvent = true;
                try {
                    GeneratedSaplingComponent generatedSapling = sapling.getComponent(GeneratedSaplingComponent.class);
                    Vector3i blockLocation = sapling.getComponent(BlockComponent.class).getPosition();
                    String saplingType = generatedSapling.type;
                    PlantGrowthDefinition plantDefinition = plantRegistry.getPlantGrowthDefinition(saplingType);
                    Long updateDelay = plantDefinition.initializeGeneratedPlant(worldProvider, blockEntityRegistry, sapling);
                    EntityRef blockEntity = blockEntityRegistry.getBlockEntityAt(blockLocation);
                    if (blockEntity.hasComponent(GeneratedSaplingComponent.class)) {
                        blockEntity.removeComponent(GeneratedSaplingComponent.class);
                    }
                    if (updateDelay != null) {
                        delayManager.addDelayedAction(blockEntity, PlantGrowingSystem.UPDATE_PLANT_ACTION_ID, updateDelay);
                    }
                } finally {
                    processingEvent = false;
                    PerformanceMonitor.endActivity();
                }
            }
        }
    }
}
