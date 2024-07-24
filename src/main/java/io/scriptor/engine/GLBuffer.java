package io.scriptor.engine;

import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;

import java.nio.ByteBuffer;

public class GLBuffer {

    private final int handle;
    private final int target;
    private final int usage;

    public GLBuffer(final int target, final int usage) {
        this.handle = glGenBuffers();
        this.target = target;
        this.usage = usage;
    }

    public GLBuffer bind() {
        glBindBuffer(target, handle);
        return this;
    }

    public GLBuffer data(final ByteBuffer data) {
        glBufferData(target, data, usage);
        return this;
    }

    public void unbind() {
        glBindBuffer(target, 0);
    }

    public void destroy() {
        glDeleteBuffers(handle);
    }
}
