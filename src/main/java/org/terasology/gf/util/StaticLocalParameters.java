// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.gf.util;

public class StaticLocalParameters implements LocalParameters {

    @Override
    public float getTemperature() {
        return 0.5f;
    }

    @Override
    public float getHumidity() {
        return 0.5f;
    }
}
