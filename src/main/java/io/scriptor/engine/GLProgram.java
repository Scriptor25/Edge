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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.function.Consumer;

public class GLProgram {

    public static record ShaderInfo(String filename, boolean isResource, int type) {
    }

    private static String readSource(final String filename, final boolean isResource) throws IOException {
        final var stream = isResource ? ClassLoader.getSystemResourceAsStream(filename) : new FileInputStream(filename);
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

    public GLProgram(final ShaderInfo... shaders) {
        handle = glCreateProgram();

        for (final var shader : shaders)
            try {
                addShader(handle, shader.type(), readSource(shader.filename(), shader.isResource()));
            } catch (IOException e) {
            }

        glLinkProgram(handle);
        glValidateProgram(handle);
    }

    public GLProgram bind() {
        glUseProgram(handle);
        return this;
    }

    public GLProgram uniform(final String name, Consumer<Integer> callback) {
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
