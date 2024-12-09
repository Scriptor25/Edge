package io.scriptor.engine.gl;

import io.scriptor.engine.IDestructible;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL15.*;

public class GLBuffer implements IDestructible {

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

    @Override
    public void destroy() {
        glDeleteBuffers(handle);
    }
}
