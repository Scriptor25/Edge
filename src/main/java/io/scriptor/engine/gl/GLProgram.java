package io.scriptor.engine.gl;

import io.scriptor.engine.IDestructible;
import io.scriptor.engine.IYamlNode;
import io.scriptor.engine.Ref;
import io.scriptor.engine.data.ProgramInfo;
import io.scriptor.engine.data.ShaderInfo;

import java.io.InputStream;
import java.util.function.IntConsumer;

import static io.scriptor.engine.data.Resources.open;
import static org.lwjgl.opengl.GL20.*;

public class GLProgram implements IDestructible {

    public static Ref<GLProgram> get(final String id) {
        return Ref.get(GLProgram.class, id);
    }

    public static Ref<GLProgram> create(final InputStream stream) {
        final var info = ProgramInfo.parse(IYamlNode.load(stream));
        return get(info.id()).set(new GLProgram(info.shaders()));
    }

    private static String readSource(final String filename) {
        return open(filename, stream -> {
            final var bytes = stream.readAllBytes();
            return new String(bytes);
        }).or("");
    }

    private static void addShader(final int program, final int shaderType, final String source) {
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

    private GLProgram(final ShaderInfo... shaders) {
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

    public GLProgram bind() {
        glUseProgram(handle);
        return this;
    }

    public GLProgram uniform(final String name, final IntConsumer callback) {
        final var location = glGetUniformLocation(handle, name);
        if (location >= 0) callback.accept(location);
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
