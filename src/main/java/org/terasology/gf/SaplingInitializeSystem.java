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

import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.engine.entitySystem.event.EventPriority;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.delay.DelayManager;
import org.terasology.engine.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.engine.monitoring.PerformanceMonitor;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.gf.generator.PlantGrowthDefinition;
import org.terasology.gf.util.LocalParameters;
import org.terasology.gf.util.StaticLocalParameters;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class SaplingInitializeSystem extends BaseComponentSystem {
    public static final String INITIALIZE_PLANT_ACTION = "PlantPack:initializePlant";
    private static final Logger logger = LoggerFactory.getLogger(SaplingInitializeSystem.class);

    @In
    private PlantRegistry plantRegistry;
    @In
    private WorldProvider worldProvider;
    @In
    private BlockEntityRegistry blockEntityRegistry;
    @In
    private DelayManager delayManager;
    @In
    private EnvironmentParametersSystem environmentSystem;

    private LocalParameters createLocalParameters(Vector3ic position) {
        if (environmentSystem != null) {
            return environmentSystem.createLocalParameters(position);
        } else {
            return new StaticLocalParameters();
        }
    }

    // To avoid stack overflow
    private boolean processingEvent;

    @ReceiveEvent
    public void generatedSaplingLoaded(OnActivatedComponent event, EntityRef sapling,
                                       GeneratedSaplingComponent generatedSaplingComponent, BlockComponent blockComponent) {
        delayManager.addDelayedAction(sapling, INITIALIZE_PLANT_ACTION, 0);
    }

    @ReceiveEvent(priority = EventPriority.PRIORITY_LOW)
    public void plantedSapling(OnActivatedComponent event, EntityRef sapling, LivingPlantComponent livingPlant,
                               PlantedSaplingComponent plantedSaplingComponent,
                               BlockComponent blockComponent) {
        if (!processingEvent) {
            processingEvent = true;
            try {
                Vector3ic blockLocation = blockComponent.getPosition();
                String saplingType = livingPlant.type;
                PlantGrowthDefinition plantDefinition = plantRegistry.getPlantGrowthDefinition(saplingType);
                Long updateDelay = plantDefinition.initializePlantedPlant(worldProvider, createLocalParameters(blockLocation), blockEntityRegistry, sapling);
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

    @ReceiveEvent
    public void delayedInitialization(DelayedActionTriggeredEvent event, EntityRef sapling,
                                      GeneratedSaplingComponent generatedSapling, BlockComponent blockComponent) {
        if (event.getActionId().equals(INITIALIZE_PLANT_ACTION)) {
            if (!processingEvent) {
                PerformanceMonitor.startActivity("GrowingFlora - Initializing sapling");
                processingEvent = true;
                try {
                    Vector3ic blockLocation = blockComponent.getPosition();
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
