// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.gf.util;

import org.joml.Vector3ic;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generation.facets.SurfaceHumidityFacet;
import org.terasology.engine.world.generation.facets.SurfaceTemperatureFacet;

public class GenerationLocalParameters implements LocalParameters {
    private Vector3ic location;
    private SurfaceTemperatureFacet surfaceTemperatureFacet;
    private SurfaceHumidityFacet surfaceHumidityFacet;

    public GenerationLocalParameters(Region chunkRegion, Vector3ic location) {
        this.location = location;
        surfaceTemperatureFacet = chunkRegion.getFacet(SurfaceTemperatureFacet.class);
        surfaceHumidityFacet = chunkRegion.getFacet(SurfaceHumidityFacet.class);
    }

    @Override
    public float getTemperature() {
        return surfaceTemperatureFacet.get(location.x(), location.z());
    }

    @Override
    public float getHumidity() {
        return surfaceHumidityFacet.get(location.x(), location.z());
    }
}
