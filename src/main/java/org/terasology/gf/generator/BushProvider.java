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
package org.terasology.gf.generator;

import org.terasology.math.geom.BaseVector3i;
import org.terasology.utilities.procedural.NoiseTable;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;

import java.util.Map;

/**
 * Determines that ground that flora can be placed on
 */
@Produces(BushFacet.class)
@Requires(@Facet(FloraFacet.class))
public class BushProvider implements FacetProvider {
    private float amount;
    private NoiseTable noise;

    public BushProvider(float amount) {
        this.amount = amount;
    }

    @Override
    public void setSeed(long seed) {
        noise = new NoiseTable(seed + 28873);
    }

    @Override
    public void process(GeneratingRegion region) {
        BushFacet facet = new BushFacet(region.getRegion(), region.getBorderForFacet(BushFacet.class));
        FloraFacet floraFacet = region.getRegionFacet(FloraFacet.class);

        for (Map.Entry<BaseVector3i, Float> positionValue : floraFacet.getRelativeEntries().entrySet()) {
            BaseVector3i pos = positionValue.getKey();
            float value = positionValue.getValue();

            if (noise.noise(pos.x(), pos.y(), pos.z()) / 256f < amount) {
                facet.set(pos, value);
            }
        }

        region.setRegionFacet(BushFacet.class, facet);
    }
}
