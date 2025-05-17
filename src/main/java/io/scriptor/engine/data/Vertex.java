package io.scriptor.engine.data;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector2fc;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

import java.nio.ByteBuffer;

public record Vertex(
        @NotNull Vector3fc position,
        @NotNull Vector2fc texture,
        @NotNull Vector3fc normal,
        @NotNull Vector4fc color
) {

    public static final int BYTES = Float.BYTES * (3 + 2 + 3 + 4);

    public void get(final @NotNull ByteBuffer buffer) {
        buffer.putFloat(position.x())
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
