package io.scriptor.engine;

import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glValidateProgram;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.IntConsumer;

import org.yaml.snakeyaml.Yaml;

public class GLProgram {

    private static final Map<String, GLProgram> instances = new HashMap<>();

    public static GLProgram get(final String id) {
        if (!instances.containsKey(id))
            throw new IllegalStateException(id);
        return instances.get(id);
    }

    public static GLProgram create(final String filename) {
        final var yaml = new Yaml();
        final var info = ProgramInfo.fromMap(yaml.load(ClassLoader.getSystemResourceAsStream(filename)));
        if (instances.containsKey(info.getId()))
            throw new IllegalStateException(info.getId());

        final var instance = new GLProgram(info.getShaders().toArray(ShaderInfo[]::new));
        instances.put(info.getId(), instance);
        return instance;
    }

    private static String readSource(final String filename) throws IOException {
        final var stream = ClassLoader.getSystemResourceAsStream(filename);
        final var bytes = stream.readAllBytes();
        return new String(bytes);
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
            try {
                addShader(handle, shader.getType(), readSource(shader.getPath()));
            } catch (final IOException e) {
                e.printStackTrace();
            }

        glLinkProgram(handle);
        glValidateProgram(handle);
    }

    public GLProgram bind() {
        glUseProgram(handle);
        return this;
    }

    public GLProgram uniform(final String name, final IntConsumer callback) {
        callback.accept(glGetUniformLocation(handle, name));
        return this;
    }

    public void unbind() {
        glUseProgram(0);
    }

    public void destroy() {
        glDeleteProgram(handle);
    }
}
