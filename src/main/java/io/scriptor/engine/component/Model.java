package io.scriptor.engine.component;

import io.scriptor.engine.Cycle;
import io.scriptor.engine.Ref;
import io.scriptor.engine.data.Material;
import io.scriptor.engine.data.Mesh;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Stream;

public class Model extends Component {

    private final @NotNull Ref<Material> material;
    private final @NotNull Ref<Mesh> @NotNull [] meshes;

    @SafeVarargs
    public Model(
            final @NotNull Cycle cycle,
            final @NotNull Ref<Material> material,
            final @NotNull Ref<Mesh> @NotNull ... meshes
    ) {
        super(cycle);

        this.material = material;
        this.meshes = meshes;

        this.material.use();
        Arrays.stream(this.meshes).forEach(Ref::use);
    }

    @SuppressWarnings("unchecked")
    public Model(
            final @NotNull Cycle cycle,
            final @NotNull String materialId,
            final @NotNull Ref<Mesh> @NotNull ... meshes
    ) {
        this(cycle, Material.get(materialId), meshes);
    }

    public Model(
            final @NotNull Cycle cycle,
            final @NotNull String materialId,
            final @NotNull String @NotNull ... meshIds
    ) {
        this(cycle, Material.get(materialId), Arrays.stream(meshIds).map(Mesh::get).<Ref<Mesh>>toArray(Ref[]::new));
    }

    public Model(final @NotNull Cycle cycle, final @NotNull String materialId, final @NotNull String meshId) {
        this(cycle, Material.get(materialId), Mesh.get(meshId));
    }

    public @NotNull Ref<Material> getMaterial() {
        return material;
    }

    public @NotNull Ref<Mesh> getMesh() {
        return meshes[0];
    }

    public @NotNull Ref<Mesh> getMesh(final int index) {
        return meshes[index];
    }

    public int getMeshCount() {
        return meshes.length;
    }

    public @NotNull Stream<Ref<Mesh>> stream() {
        return Arrays.stream(meshes);
    }

    @Override
    public void destroy() {
        material.drop();
        Arrays.stream(meshes).forEach(Ref::drop);
    }
}
