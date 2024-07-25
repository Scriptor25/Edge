package io.scriptor.engine;

import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;

public class ShaderInfo {

    public String path;
    public String type;

    public int getType() {
        return switch (type) {
            case "vertex" -> GL_VERTEX_SHADER;
            case "fragment" -> GL_FRAGMENT_SHADER;
            default -> throw new IllegalStateException(type);
        };
    }
}
