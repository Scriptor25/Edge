package io.scriptor.engine.data;

import io.scriptor.engine.IYamlNode;
import org.jetbrains.annotations.NotNull;

import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;
import static org.lwjgl.opengl.GL40.GL_TESS_CONTROL_SHADER;
import static org.lwjgl.opengl.GL40.GL_TESS_EVALUATION_SHADER;
import static org.lwjgl.opengl.GL43.GL_COMPUTE_SHADER;

public record ShaderInfo(@NotNull String path, int type) {

    public static @NotNull ShaderInfo parse(final @NotNull IYamlNode yaml) {
        final var path = yaml.get("path").as(String.class).get();
        final var type = yaml.get("type").as(String.class).get();
        return new ShaderInfo(path, switch (type) {
            case "vertex" -> GL_VERTEX_SHADER;
            case "fragment" -> GL_FRAGMENT_SHADER;
            case "geometry" -> GL_GEOMETRY_SHADER;
            case "compute" -> GL_COMPUTE_SHADER;
            case "tess-control" -> GL_TESS_CONTROL_SHADER;
            case "tess-evaluation" -> GL_TESS_EVALUATION_SHADER;
            default -> throw new IllegalStateException(type);
        });
    }
}
