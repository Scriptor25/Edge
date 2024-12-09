package io.scriptor.engine.gl;

import io.scriptor.engine.IDestructible;
import io.scriptor.engine.IYamlNode;
import io.scriptor.engine.data.ProgramInfo;
import io.scriptor.engine.data.ShaderInfo;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntConsumer;

import static io.scriptor.engine.data.Resources.open;
import static org.lwjgl.opengl.GL20.*;

public class GLProgram implements IDestructible {

    private static final Map<String, GLProgram> instances = new HashMap<>();

    public static GLProgram get(final String id) {
        if (!instances.containsKey(id))
            throw new IllegalStateException(id);
        return instances.get(id);
    }

    public static GLProgram create(final InputStream stream) {
        final var info = ProgramInfo.parse(IYamlNode.load(stream));
        if (instances.containsKey(info.id()))
            throw new IllegalStateException(info.id());

        final var instance = new GLProgram(info.shaders());
        instances.put(info.id(), instance);
        return instance;
    }

    private static String readSource(final String filename) {
        return open(filename, stream -> {
            final var bytes = stream.readAllBytes();
            return new String(bytes);
        }).orElse("");
    }

    private static void addShader(final int program, final int shaderType, final CharSequence source) {
        final var shader = glCreateShader(shaderType);
        glShaderSource(shader, source);
        glCompileShader(shader);
        glAttachShader(program, shader);
        glDeleteShader(shader);
    }

    private final int handle;

    private GLProgram(final ShaderInfo... shaders) {
        handle = glCreateProgram();

        for (final var shader : shaders)
            addShader(handle, shader.getType(), readSource(shader.getPath()));

        glLinkProgram(handle);
        glValidateProgram(handle);
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
