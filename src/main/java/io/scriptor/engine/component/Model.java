package io.scriptor.engine.component;

import java.util.Arrays;
import java.util.stream.Stream;

import io.scriptor.engine.Cycle;
import io.scriptor.engine.Material;
import io.scriptor.engine.Mesh;

public class Model extends Component {

    private final Material material;
    private final Mesh[] meshes;

    public Model(final Cycle cycle, final Material material, final Mesh... meshes) {
        super(cycle);

        this.material = material;
        this.meshes = meshes;

        material.use();
        for (final var mesh : meshes)
            mesh.use();
    }

    public Model(final Cycle cycle, final String materialId, final Mesh... meshes) {
        this(cycle, Material.get(materialId), meshes);
    }

    public Model(final Cycle cycle, final String materialid, final String... meshIds) {
        this(cycle, Material.get(materialid), Arrays.stream(meshIds).map(Mesh::get).toArray(Mesh[]::new));
    }

    public Model(final Cycle cycle, final String materialid, final String meshId) {
        this(cycle, Material.get(materialid), Mesh.get(meshId));
    }

    public Material getMaterial() {
        return material;
    }

    public Mesh getMesh() {
        return meshes[0];
    }

    public Mesh getMesh(final int index) {
        return meshes[index];
    }

    public int getMeshCount() {
        return meshes.length;
    }

    public Stream<Mesh> stream() {
        return Arrays.stream(meshes);
    }

    @Override
    public void destroy() {
        material.drop();
        material.destroy();
        for (final var mesh : meshes) {
            mesh.drop();
            mesh.destroy();
        }
    }
}
