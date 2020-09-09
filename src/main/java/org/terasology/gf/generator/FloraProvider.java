// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.gf.generator;

import org.terasology.engine.utilities.procedural.NoiseTable;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.FacetBorder;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.Requires;
import org.terasology.engine.world.generation.facets.DensityFacet;
import org.terasology.engine.world.generation.facets.SurfaceHeightFacet;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector3i;

/**
 * Determines that ground that flora can be placed on
 */
@Produces(FloraFacet.class)
@Requires({
        @Facet(value = SurfaceHeightFacet.class, border = @FacetBorder(sides = 5)),
        @Facet(value = DensityFacet.class, border = @FacetBorder(top = 1, sides = 5))})
public class FloraProvider implements FacetProvider {

    private final int seaLevel;
    private NoiseTable noiseTable;

    public FloraProvider(int seaLevel) {
        this.seaLevel = seaLevel;
    }

    @Override
    public void setSeed(long seed) {
        noiseTable = new NoiseTable(seed);
    }

    @Override
    public void process(GeneratingRegion region) {
        FloraFacet facet = new FloraFacet(region.getRegion(), region.getBorderForFacet(FloraFacet.class));
        SurfaceHeightFacet surface = region.getRegionFacet(SurfaceHeightFacet.class);
        DensityFacet density = region.getRegionFacet(DensityFacet.class);

        int minX = facet.getWorldRegion().minX();
        int minZ = facet.getWorldRegion().minZ();
        int maxX = facet.getWorldRegion().maxX();
        int maxZ = facet.getWorldRegion().maxZ();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int height = TeraMath.floorToInt(surface.getWorld(x, z));
                // if the surface is in range, and if we are above sea level
                if (facet.getWorldRegion().encompasses(x, height, z) && facet.getWorldRegion().encompasses(x,
                        height + 1, z) && height >= seaLevel) {

                    // if the block on the surface is dense enough
                    if (density.getWorld(x, height, z) >= 0
                            && density.getWorld(x, height + 1, z) < 0) {
                        facet.setFlag(new Vector3i(x, height, z), noiseTable.noise(x, z) / 256f);
                    }
                }
            }
        }

        region.setRegionFacet(FloraFacet.class, facet);
    }
}
