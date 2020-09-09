// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gf.generator;

import org.terasology.engine.utilities.procedural.NoiseTable;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.Requires;
import org.terasology.math.geom.Vector3i;

import java.util.Map;

/**
 * Determines that ground that flora can be placed on
 */
@Produces(TreeFacet.class)
@Requires(@Facet(FloraFacet.class))
public class TreeProvider implements FacetProvider {
    private final float amount;
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
