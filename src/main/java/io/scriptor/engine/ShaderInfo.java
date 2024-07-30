package io.scriptor.engine;

import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;

import java.util.Map;

public class ShaderInfo {

    public static ShaderInfo fromMap(final Map<String, Object> map) {
        final var path = (String) map.get("path");
        final var type = (String) map.get("type");
        return new ShaderInfo(path, type);
    }

    private final String path;
    private final String type;

    public ShaderInfo(final String path, final String type) {
        this.path = path;
        this.type = type;
    }

    public String getPath() {
        return path;
    }

    public int getType() {
        return switch (type) {
            case "vertex" -> GL_VERTEX_SHADER;
            case "fragment" -> GL_FRAGMENT_SHADER;
            default -> throw new IllegalStateException(type);
        };
    }
}
