package io.scriptor.engine.gl;

import io.scriptor.engine.IDestructible;
import io.scriptor.engine.IYamlNode;
import io.scriptor.engine.Ref;
import io.scriptor.engine.data.IUniform;
import io.scriptor.engine.data.ProgramInfo;
import io.scriptor.engine.data.ShaderInfo;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;

import static io.scriptor.engine.data.Resources.open;
import static org.lwjgl.opengl.GL20.*;

public class GLProgram implements IDestructible {

    public static @NotNull Ref<GLProgram> get(final @NotNull String id) {
        return Ref.get(GLProgram.class, id);
    }

    public static @NotNull Ref<GLProgram> create(final @NotNull InputStream stream) {
        final var info = ProgramInfo.parse(IYamlNode.load(stream));
        return get(info.id()).set(new GLProgram(info.shaders()));
    }

    private static @NotNull String readSource(final @NotNull String filename) {
        return open(filename, stream -> {
            final var bytes = stream.readAllBytes();
            return new String(bytes);
        }).or("");
    }

    private static void addShader(final int program, final int shaderType, final @NotNull String source) {
        final var shader = glCreateShader(shaderType);
        glShaderSource(shader, source);
        glCompileShader(shader);

        final var pStatus = new int[1];
        glGetShaderiv(shader, GL_COMPILE_STATUS, pStatus);
        if (pStatus[0] != GL_TRUE) {
            final var message = glGetShaderInfoLog(shader);
            System.err.println(message);
            glDeleteShader(shader);
            return;
        }

        glAttachShader(program, shader);
        glDeleteShader(shader);
    }

    private final int handle;

    private GLProgram(final @NotNull ShaderInfo @NotNull ... shaders) {
        handle = glCreateProgram();

        for (final var shader : shaders)
            addShader(handle, shader.type(), readSource(shader.path()));

        final var pStatus = new int[1];

        glLinkProgram(handle);

        glGetProgramiv(handle, GL_LINK_STATUS, pStatus);
        if (pStatus[0] != GL_TRUE) {
            final var message = glGetProgramInfoLog(handle);
            System.err.println(message);
            glDeleteProgram(handle);
            return;
        }

        glValidateProgram(handle);

        glGetProgramiv(handle, GL_VALIDATE_STATUS, pStatus);
        if (pStatus[0] != GL_TRUE) {
            final var message = glGetProgramInfoLog(handle);
            System.err.println(message);
            glDeleteProgram(handle);
        }
    }

    public @NotNull GLProgram bind() {
        glUseProgram(handle);
        return this;
    }

    public @NotNull GLProgram uniform(final @NotNull String name, final @NotNull IUniform callback) {
        final var location = glGetUniformLocation(handle, name);
        if (location >= 0)
            callback.apply(handle, location);
        return this;
    }

    public void unbind() {
        glUseProgram(0);
    }

    @Override
    public void destroy() {
        glDeleteProgram(handle);
    }
}
