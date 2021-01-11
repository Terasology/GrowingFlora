// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gf.generator;

import org.joml.Vector3i;
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
@Produces(TreeFacet.class)
@Requires(@Facet(FloraFacet.class))
public class TreeProvider implements FacetProvider {
    private float amount;
    private NoiseTable noise;

    public TreeProvider(float amount) {
        this.amount = amount;
    }

    @Override
    public void setSeed(long seed) {
        noise = new NoiseTable(seed + 26873);
    }

    @Override
    public void process(GeneratingRegion region) {
        TreeFacet facet = new TreeFacet(region.getRegion(), region.getBorderForFacet(TreeFacet.class));
        FloraFacet floraFacet = region.getRegionFacet(FloraFacet.class);

        for (Map.Entry<Vector3i, Float> positionValue : floraFacet.getFlaggedPositions().entrySet()) {
            Vector3i pos = positionValue.getKey();
            float value = positionValue.getValue();

            if (noise.noise(pos.x, pos.y, pos.z) / 256f < amount) {
                facet.setFlag(pos, value);
            }
        }

        region.setRegionFacet(TreeFacet.class, facet);
    }
}
