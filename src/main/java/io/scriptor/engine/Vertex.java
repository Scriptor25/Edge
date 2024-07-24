package io.scriptor.engine;

import java.nio.ByteBuffer;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

public class Vertex {

    public static final int BYTES = Float.BYTES * (3 + 3 + 4);

    public final Vector3f position = new Vector3f();
    public final Vector3f normal = new Vector3f();
    public final Vector4f color = new Vector4f();

    public Vertex(final Vector3fc position, final Vector3fc normal, final Vector4fc color) {
        this.position.set(position);
        this.normal.set(normal);
        this.color.set(color);
    }

    public void get(final ByteBuffer buffer) {
        buffer
                .putFloat(position.x())
                .putFloat(position.y())
                .putFloat(position.z())
                .putFloat(normal.x())
                .putFloat(normal.y())
                .putFloat(normal.z())
                .putFloat(color.x())
                .putFloat(color.y())
                .putFloat(color.z())
                .putFloat(color.w());
    }
}
