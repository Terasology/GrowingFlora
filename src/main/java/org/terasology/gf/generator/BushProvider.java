// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gf.generator;

import org.joml.Vector3i;
import org.terasology.engine.utilities.procedural.NoiseTable;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.Requires;

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

        for (Map.Entry<Vector3i, Float> positionValue : floraFacet.getFlaggedPositions().entrySet()) {
            Vector3i pos = positionValue.getKey();
            float value = positionValue.getValue();

            if (noise.noise(pos.x, pos.y, pos.z) / 256f < amount) {
                facet.setFlag(pos, value);
            }
        }

        region.setRegionFacet(BushFacet.class, facet);
    }
}
