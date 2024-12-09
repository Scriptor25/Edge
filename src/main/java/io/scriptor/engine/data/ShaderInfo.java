package io.scriptor.engine.data;

import io.scriptor.engine.IYamlNode;

import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;

public class ShaderInfo {

    public static ShaderInfo parse(final IYamlNode yaml) {
        final var path = yaml.get("path").as(String.class).orElseThrow();
        final var type = yaml.get("type").as(String.class).orElseThrow();
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
