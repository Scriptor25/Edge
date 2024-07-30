package io.scriptor.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProgramInfo {

    public static ProgramInfo fromMap(final Map<String, Object> map) {
        final var id = (String) map.get("id");
        @SuppressWarnings("unchecked")
        final var shaderMaps = (List<Map<String, Object>>) map.get("shaders");
        final List<ShaderInfo> shaders = new ArrayList<>();
        for (final var shaderMap : shaderMaps) {
            final var shader = ShaderInfo.fromMap(shaderMap);
            shaders.add(shader);
        }
        return new ProgramInfo(id, shaders);
    }

    private final String id;
    private final List<ShaderInfo> shaders;

    public ProgramInfo(final String id, final List<ShaderInfo> shaders) {
        this.id = id;
        this.shaders = shaders;
    }

    public String getId() {
        return id;
    }

    public List<ShaderInfo> getShaders() {
        return shaders;
    }
}
