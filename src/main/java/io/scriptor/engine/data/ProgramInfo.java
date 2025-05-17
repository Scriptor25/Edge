package io.scriptor.engine.data;

import io.scriptor.engine.IYamlNode;
import org.jetbrains.annotations.NotNull;

public record ProgramInfo(@NotNull String id, @NotNull ShaderInfo @NotNull [] shaders) {

    public static @NotNull ProgramInfo parse(final @NotNull IYamlNode node) {
        final var id = node
                .get("id")
                .as(String.class)
                .get();
        final var shaders = node
                .get("shaders")
                .stream()
                .map(ShaderInfo::parse)
                .toArray(ShaderInfo[]::new);
        return new ProgramInfo(id, shaders);
    }
}
