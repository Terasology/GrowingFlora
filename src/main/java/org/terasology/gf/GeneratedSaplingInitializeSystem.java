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
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.gf.generator.PlantGrowthDefinition;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockComponent;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class GeneratedSaplingInitializeSystem implements UpdateSubscriberSystem {
    private static final Logger logger = LoggerFactory.getLogger(GeneratedSaplingInitializeSystem.class);
    private static final long CHECK_INTERVAL = 100;
    private long lastCheck;

    @In
    private PlantRegistry plantRegistry;
    @In
    private EntityManager entityManager;
    @In
    private Time time;

    @Override
    public void initialise() {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void update(float delta) {
        long currentTime = time.getGameTimeInMs();
        if (lastCheck + CHECK_INTERVAL <= currentTime) {
            WorldProvider worldProvider = CoreRegistry.get(WorldProvider.class);
            BlockEntityRegistry blockEntityRegistry = CoreRegistry.get(BlockEntityRegistry.class);
            for (EntityRef sapling : entityManager.getEntitiesWith(GeneratedSaplingComponent.class, BlockComponent.class)) {
                GeneratedSaplingComponent generatedSapling = sapling.getComponent(GeneratedSaplingComponent.class);
                if (generatedSapling != null) {
                    String saplingType = generatedSapling.type;
                    PlantGrowthDefinition plantDefinition = plantRegistry.getPlantGrowthDefinition(saplingType);
                    if (plantDefinition.initializePlant(worldProvider, blockEntityRegistry, sapling)) {
                        sapling.removeComponent(GeneratedSaplingComponent.class);
                    }
                }
            }

            lastCheck = currentTime;
        }
    }
}
