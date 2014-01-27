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
package org.terasology.gf.tree;

import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.registry.Share;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
@RegisterSystem
@Share(value = PlantRegistry.class)
public class PlantRegistryImpl extends BaseComponentSystem implements PlantRegistry {
    private Map<String, PlantDefinition> trees = new HashMap<>();

    @Override
    public void addTreeType(String treeId, PlantDefinition plantDefinition) {
        trees.put(treeId, plantDefinition);
    }

    @Override
    public PlantDefinition getPlantDefinition(String plantId) {
        return trees.get(plantId);
    }
}
