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
package org.terasology.gf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.gf.generator.PlantGrowthDefinition;
import org.terasology.logic.delay.AddDelayedActionEvent;
import org.terasology.registry.In;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockComponent;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class GeneratedSaplingInitializeSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(GeneratedSaplingInitializeSystem.class);

    @In
    private PlantRegistry plantRegistry;
    @In
    private WorldProvider worldProvider;
    @In
    private BlockEntityRegistry blockEntityRegistry;

    // To avoid stack overflow
    private boolean processingEvent;

    @ReceiveEvent(components = {GeneratedSaplingComponent.class, BlockComponent.class})
    public void generatedSaplingLoaded(OnActivatedComponent event, EntityRef sapling) {
        if (!processingEvent) {
            processingEvent = true;
            try {
                GeneratedSaplingComponent generatedSapling = sapling.getComponent(GeneratedSaplingComponent.class);
                if (generatedSapling != null) {
                    String saplingType = generatedSapling.type;
                    PlantGrowthDefinition plantDefinition = plantRegistry.getPlantGrowthDefinition(saplingType);
                    Long updateDelay = plantDefinition.initializePlant(worldProvider, blockEntityRegistry, sapling);
                    if (updateDelay == null) {
                        sapling.removeComponent(GeneratedSaplingComponent.class);
                    } else {
                        sapling.send(new AddDelayedActionEvent(PlantGrowingSystem.UPDATE_PLANT_ACTION_ID, updateDelay));
                    }
                }
            } finally {
                processingEvent = false;
            }
        }
    }
}
