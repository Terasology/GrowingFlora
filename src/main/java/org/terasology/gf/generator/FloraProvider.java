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

import org.terasology.math.geom.Vector3i;
import org.terasology.utilities.procedural.NoiseTable;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetBorder;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.SurfacesFacet;

/**
 * Determines that ground that flora can be placed on
 */
@Produces(FloraFacet.class)
@Requires(@Facet(value = SurfacesFacet.class, border = @FacetBorder(bottom = 1)))
public class FloraProvider implements FacetProvider {

    private NoiseTable noiseTable;
    private int seaLevel;

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
        SurfacesFacet surface = region.getRegionFacet(SurfacesFacet.class);

        int minX = facet.getWorldRegion().minX();
        int minZ = facet.getWorldRegion().minZ();
        int maxX = facet.getWorldRegion().maxX();
        int maxZ = facet.getWorldRegion().maxZ();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int height  : surface.getWorldColumn(x, z)) {
                    // if the surface is in range, and if we are above sea level
                    if (facet.getWorldRegion().contains(x, height, z) && facet.getWorldRegion().contains(x, height + 1, z) && height >= seaLevel) {
                        facet.setFlag(new Vector3i(x, height, z), noiseTable.noise(x, z) / 256f);
                    }
                }
            }
        }

        region.setRegionFacet(FloraFacet.class, facet);
    }
}
