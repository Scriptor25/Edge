package io.scriptor.engine.data;

import org.joml.Vector2fc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

import java.nio.ByteBuffer;

public record Vertex(Vector3fc position, Vector2fc texture, Vector3fc normal, Vector4fc color) {

    public static final int BYTES = Float.BYTES * (3 + 2 + 3 + 4);

    public void get(final ByteBuffer buffer) {
        buffer
                .putFloat(position.x())
                .putFloat(position.y())
                .putFloat(position.z())
                .putFloat(texture.x())
                .putFloat(texture.y())
                .putFloat(normal.x())
                .putFloat(normal.y())
                .putFloat(normal.z())
                .putFloat(color.x())
                .putFloat(color.y())
                .putFloat(color.z())
                .putFloat(color.w());
    }
}
