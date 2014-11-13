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

import org.terasology.math.TeraMath;
import org.terasology.math.Vector3i;
import org.terasology.utilities.procedural.NoiseTable;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetBorder;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.DensityFacet;
import org.terasology.world.generation.facets.SeaLevelFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

/**
 * Determines that ground that flora can be placed on
 */
@Produces(FloraFacet.class)
@Requires({@Facet(SeaLevelFacet.class),
        @Facet(value = SurfaceHeightFacet.class, border = @FacetBorder(sides = 5)),
        @Facet(value = DensityFacet.class, border = @FacetBorder(top = 1, sides = 5))})
public class FloraProvider implements FacetProvider {

    private NoiseTable noiseTable;

    @Override
    public void setSeed(long seed) {
        noiseTable = new NoiseTable(seed);
    }

    @Override
    public void process(GeneratingRegion region) {
        FloraFacet facet = new FloraFacet(region.getRegion(), region.getBorderForFacet(FloraFacet.class));
        SurfaceHeightFacet surface = region.getRegionFacet(SurfaceHeightFacet.class);
        DensityFacet density = region.getRegionFacet(DensityFacet.class);
        SeaLevelFacet seaLevel = region.getRegionFacet(SeaLevelFacet.class);

        int minX = facet.getWorldRegion().minX();
        int minZ = facet.getWorldRegion().minZ();
        int maxX = facet.getWorldRegion().maxX();
        int maxZ = facet.getWorldRegion().maxZ();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                int height = TeraMath.floorToInt(surface.getWorld(x, z));
                // if the surface is in range, and if we are above sea level
                if (facet.getWorldRegion().encompasses(x, height, z) && facet.getWorldRegion().encompasses(x, height + 1, z) && height >= seaLevel.getSeaLevel()) {

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
