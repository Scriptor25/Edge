package io.scriptor.engine.data;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector4fc;

public record MaterialInfo(
        @NotNull String name,
        int shadingMode,
        int illumination,
        @NotNull Vector4fc ambient,
        @NotNull Vector4fc diffuse,
        @NotNull Vector4fc specular,
        @NotNull Vector4fc emissive,
        float shininess,
        float opacity,
        @NotNull Vector4fc transparent,
        float anisotropyFactor,
        float refractionIndex
) {
}
