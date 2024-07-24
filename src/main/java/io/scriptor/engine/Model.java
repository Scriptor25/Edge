package io.scriptor.engine;

public class Model {

    private final Mesh mesh;
    private final Material material;

    public Model(final Mesh mesh, final Material material) {
        mesh.use(this);
        material.use(this);

        this.mesh = mesh;
        this.material = material;
    }

    public Mesh getMesh() {
        return mesh;
    }

    public Material getMaterial() {
        return material;
    }
}
