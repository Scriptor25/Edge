package io.scriptor.engine.data;

import io.scriptor.engine.Ref;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public record MeshInfo(@NotNull String name, @NotNull Vertex @NotNull [] vertices, int @NotNull [] indices) {

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (obj instanceof MeshInfo info)
            return name.equals(info.name)
                   && Arrays.equals(vertices, info.vertices)
                   && Arrays.equals(indices, info.indices);
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, Arrays.hashCode(vertices), Arrays.hashCode(indices));
    }

    @Override
    public @NotNull String toString() {
        return "MeshInfo[name=%s, vertices=%s, indices=%s]"
                .formatted(
                        name,
                        Arrays.toString(vertices),
                        Arrays.toString(indices));
    }

    public @NotNull Ref<Mesh> create() {
        final var ref = Mesh.create(name);
        ref.ok(mesh -> {
            mesh.add(indices);
            mesh.add(vertices);
            mesh.apply();
        });
        return ref;
    }
}
