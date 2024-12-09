package io.scriptor.engine.data;

import io.scriptor.engine.IYamlNode;

public record ProgramInfo(String id, ShaderInfo[] shaders) {

    public static ProgramInfo parse(final IYamlNode node) {
        final var id = node
                .get("id")
                .as(String.class)
                .orElseThrow();
        final var shaders = node
                .get("shaders")
                .stream()
                .map(ShaderInfo::parse)
                .toArray(ShaderInfo[]::new);
        return new ProgramInfo(id, shaders);
    }
}
