package io.scriptor.engine.component;

import io.scriptor.engine.Cycle;
import io.scriptor.engine.Ref;
import io.scriptor.engine.data.Material;
import io.scriptor.engine.data.Mesh;

import java.util.Arrays;
import java.util.stream.Stream;

public class Model extends Component {

    private final Ref<Material> material;
    private final Ref<Mesh>[] meshes;

    @SafeVarargs
    public Model(final Cycle cycle, final Ref<Material> material, final Ref<Mesh>... meshes) {
        super(cycle);

        this.material = material;
        this.meshes = meshes;

        this.material.use();
        Arrays.stream(this.meshes).forEach(Ref::use);
    }

    @SuppressWarnings("unchecked")
    public Model(final Cycle cycle, final String materialId, final Ref<Mesh>... meshes) {
        this(cycle, Material.get(materialId), meshes);
    }

    public Model(final Cycle cycle, final String materialId, final String... meshIds) {
        this(cycle, Material.get(materialId), Arrays.stream(meshIds).map(Mesh::get).<Ref<Mesh>>toArray(Ref[]::new));
    }

    public Model(final Cycle cycle, final String materialId, final String meshId) {
        this(cycle, Material.get(materialId), Mesh.get(meshId));
    }

    public Ref<Material> getMaterial() {
        return material;
    }

    public Ref<Mesh> getMesh() {
        return meshes[0];
    }

    public Ref<Mesh> getMesh(final int index) {
        return meshes[index];
    }

    public int getMeshCount() {
        return meshes.length;
    }

    public Stream<Ref<Mesh>> stream() {
        return Arrays.stream(meshes);
    }

    @Override
    public void destroy() {
        material.drop();
        Arrays.stream(meshes).forEach(Ref::drop);
    }
}
