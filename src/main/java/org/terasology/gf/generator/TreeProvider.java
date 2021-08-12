// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gf.generator;

import org.joml.Vector3ic;
import org.terasology.engine.utilities.procedural.WhiteNoise;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.FacetBorder;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.Requires;

import java.util.Map;

/**
 * Determines that ground that flora can be placed on
 */
@Produces(TreeFacet.class)
@Requires(@Facet(value = FloraFacet.class, border = @FacetBorder(sides = 13, bottom = 35)))
public class TreeProvider implements FacetProvider {
    private float amount;
    private WhiteNoise noise;

    public TreeProvider(float amount) {
        this.amount = amount;
    }

    @Override
    public void setSeed(long seed) {
        noise = new WhiteNoise(seed + 26873);
    }

    @Override
    public void process(GeneratingRegion region) {
        Border3D border = region.getBorderForFacet(TreeFacet.class);
        TreeFacet facet = new TreeFacet(region.getRegion(), border.extendBy(0, 35, 13));
        FloraFacet floraFacet = region.getRegionFacet(FloraFacet.class);

        for (Map.Entry<Vector3ic, Float> positionValue : floraFacet.getWorldEntries().entrySet()) {
            Vector3ic pos = positionValue.getKey();
            float value = positionValue.getValue();

            if (noise.noise(pos.x(), pos.y(), pos.z()) / 256f < amount) {
                facet.setWorld(pos, value);
            }
        }

        region.setRegionFacet(TreeFacet.class, facet);
    }
}
