package io.scriptor.engine.data;

import org.joml.Vector4fc;

public record MaterialInfo(
        String name,
        int shadingMode,
        int illumination,
        Vector4fc ambient,
        Vector4fc diffuse,
        Vector4fc specular,
        Vector4fc emissive,
        float shininess,
        float opacity,
        Vector4fc transparent,
        float anisotropyFactor,
        float refractionIndex) {
}
