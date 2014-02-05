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
import org.terasology.engine.Time;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.gf.generator.PlantGrowthDefinition;
import org.terasology.logic.delay.AddDelayedActionEvent;
import org.terasology.logic.delay.DelayedActionTriggeredEvent;
import org.terasology.registry.In;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockComponent;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class PlantGrowingSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(PlantGrowingSystem.class);
    public static final String UPDATE_PLANT_ACTION_ID = "GrowingFlora:updatePlant";

    @In
    private WorldProvider worldProvider;
    @In
    private EntityManager entityManager;
    @In
    private BlockEntityRegistry blockEntityRegistry;
    @In
    private Time time;
    @In
    private PlantRegistry plantRegistry;

    @ReceiveEvent(components = {LivingPlantComponent.class, BlockComponent.class})
    public void updatePlant(DelayedActionTriggeredEvent event, EntityRef plant) {
        if (event.getActionId().equals(UPDATE_PLANT_ACTION_ID)) {
            LivingPlantComponent plantComponent = plant.getComponent(LivingPlantComponent.class);
            PlantGrowthDefinition plantDefinition = plantRegistry.getPlantGrowthDefinition(plantComponent.type);
            Long updateDelay = plantDefinition.updatePlant(worldProvider, blockEntityRegistry, plant);
            if (updateDelay != null) {
                plant.send(new AddDelayedActionEvent(UPDATE_PLANT_ACTION_ID, updateDelay));
            }
        }
    }
}
