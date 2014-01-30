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
package org.terasology.gf.generator;

import org.terasology.anotherWorld.GenerationParameters;
import org.terasology.gf.PlantType;
import org.terasology.math.Vector3i;
import org.terasology.world.ChunkView;
import org.terasology.world.generator.plugin.WorldGeneratorPlugin;

/**
 * @author Marcin Sciesinski <marcins78@gmail.com>
 */
public interface PlantSpawnDefinition extends WorldGeneratorPlugin {
    PlantType getPlantType();

    String getBiomeId();

    float getRarity();

    float getProbability();

    void generatePlant(String seed, Vector3i chunkPos, ChunkView chunk, int x, int y, int z, GenerationParameters generationParameters);
}
