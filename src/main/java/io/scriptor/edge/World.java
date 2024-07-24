package io.scriptor.edge;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.yaml.snakeyaml.Yaml;

import io.scriptor.engine.Mesh;

public class World {

    public static World load(final InputStream stream) {
        final var yaml = new Yaml();
        final Map<String, Object> map = yaml.load(stream);
        return fromMap(map);
    }

    @SuppressWarnings("unchecked")
    public static World fromMap(final Map<String, Object> map) {
        final var id = (String) map.get("id");
        final var name = (String) map.get("name");
        final var boundsData = (List<Integer>) map.get("bounds");
        final var spawnData = (List<Integer>) map.get("spawn");
        final var prismsData = (List<List<Integer>>) map.get("prisms");
        final var bounds = new Vec3<Integer>(boundsData.get(0), boundsData.get(1), boundsData.get(2));
        final var spawn = new Vec3<Integer>(spawnData.get(0), spawnData.get(1), spawnData.get(2));
        final var prisms = (Vec3<Integer>[]) prismsData.stream()
                .map(position -> new Vec3<>(position.get(0), position.get(1), position.get(2)))
                .toArray(Vec3[]::new);
        final var world = new World(id, name, bounds, spawn, prisms);
        final var voxels = (List<List<List<Integer>>>) map.get("voxels");
        for (int z = 0; z < bounds.z; ++z)
            for (int y = 0; y < bounds.y; ++y)
                for (int x = 0; x < bounds.x; ++x) {
                    final var type = voxels.get(y).get(bounds.z - z - 1).get(x);
                    if (type < 0)
                        continue;
                    world.set(x, y, z, VoxelType.values()[type]);
                }
        return world;
    }

    public enum VoxelType {
        Base,
        PulseNorth,
        PulseSouth,
        PulseEast,
        PulseWest,
        Shrink,
        Magnify,
        Push,
        EndFrame,
        EndCenter
    }

    private final String id;
    private final String name;
    private final Vec3<Integer> bounds;
    private final Vec3<Integer> spawn;
    private final Vec3<Integer>[] prisms;
    private final VoxelType[][][] voxels;

    public World(final String id, final String name, final Vec3<Integer> bounds, final Vec3<Integer> spawn,
            final Vec3<Integer>[] prisms) {
        this.id = id;
        this.name = name;
        this.bounds = bounds;
        this.spawn = spawn;
        this.prisms = prisms;
        this.voxels = new VoxelType[bounds.z][bounds.y][bounds.x];
    }

    public String getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Vec3<Integer> getBounds() {
        return bounds;
    }

    public Vec3<Integer> getSpawn() {
        return spawn;
    }

    public int getPrismCount() {
        return prisms.length;
    }

    public Vec3<Integer> getPrism(final int index) {
        return prisms[index];
    }

    public Stream<Vec3<Integer>> getPrisms() {
        return Arrays.stream(prisms);
    }

    public VoxelType get(final int x, final int y, final int z) {
        if (x < 0 || x >= bounds.x || y < 0 || y >= bounds.y || z < 0 || z >= bounds.z)
            return null;
        return voxels[z][y][x];
    }

    public void set(final int x, final int y, final int z, final VoxelType voxel) {
        if (x < 0 || x >= bounds.x || y < 0 || y >= bounds.y || z < 0 || z >= bounds.z)
            return;
        voxels[z][y][x] = voxel;
    }

    public boolean isAir(final int x, final int y, final int z) {
        return get(x, y, z) == null;
    }

    public void generate(final Mesh defaultMesh, final Mesh... meshes) {
        defaultMesh.clear();
        for (final var mesh : meshes)
            if (mesh != null)
                mesh.clear();

        final var right = new Vector3f(1.0f, 0.0f, 0.0f);
        final var up = new Vector3f(0.0f, 1.0f, 0.0f);
        final var upHalf = new Vector3f(0.0f, 0.5f, 0.0f);
        final var forward = new Vector3f(0.0f, 0.0f, 1.0f);

        final var baseColor = new Vector4f(0.8f, 0.8f, 0.8f, 1.0f);
        final var checkerColor = new Vector4f(0.6f, 0.6f, 0.6f, 1.0f);
        final var otherColor = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

        for (int z = 0; z < bounds.z; ++z)
            for (int y = 0; y < bounds.y; ++y)
                for (int x = 0; x < bounds.x; ++x) {
                    final var type = get(x, y, z);
                    if (type == null)
                        continue;

                    var mesh = meshes[type.ordinal()];
                    if (mesh == null)
                        mesh = defaultMesh;

                    final Vector4f color;
                    switch (type) {
                        case Base:
                            final var isEven = (x + y + z) % 2 == 0;
                            color = isEven ? baseColor : checkerColor;
                            break;

                        default:
                            color = otherColor;
                            break;
                    }

                    if (y == 0) {
                        if (isAir(x - 1, y, z))
                            mesh.addQuad(new Vector3f(x - 0.5f, y, z - 0.5f), upHalf, forward, baseColor);
                        if (isAir(x + 1, y, z))
                            mesh.addQuad(new Vector3f(x + 0.5f, y, z - 0.5f), forward, upHalf, baseColor);
                        mesh.addQuad(new Vector3f(x - 0.5f, y, z - 0.5f), forward, right, baseColor);
                        if (isAir(x, y + 1, z))
                            mesh.addQuad(new Vector3f(x - 0.5f, y + 0.5f, z - 0.5f), right, forward, color);
                        if (isAir(x, y, z - 1))
                            mesh.addQuad(new Vector3f(x - 0.5f, y, z - 0.5f), right, upHalf, baseColor);
                        if (isAir(x, y, z + 1))
                            mesh.addQuad(new Vector3f(x - 0.5f, y, z + 0.5f), upHalf, right, baseColor);
                    } else {
                        if (isAir(x - 1, y, z))
                            mesh.addQuad(new Vector3f(x - 0.5f, y - 0.5f, z - 0.5f), up, forward, baseColor);
                        if (isAir(x + 1, y, z))
                            mesh.addQuad(new Vector3f(x + 0.5f, y - 0.5f, z - 0.5f), forward, up, baseColor);
                        if (isAir(x, y - 1, z))
                            mesh.addQuad(new Vector3f(x - 0.5f, y - 0.5f, z - 0.5f), forward, right, baseColor);
                        if (isAir(x, y + 1, z))
                            mesh.addQuad(new Vector3f(x - 0.5f, y + 0.5f, z - 0.5f), right, forward, color);
                        if (isAir(x, y, z - 1))
                            mesh.addQuad(new Vector3f(x - 0.5f, y - 0.5f, z - 0.5f), right, up, baseColor);
                        if (isAir(x, y, z + 1))
                            mesh.addQuad(new Vector3f(x - 0.5f, y - 0.5f, z + 0.5f), up, right, baseColor);
                    }
                }

        defaultMesh.apply();
        for (final var mesh : meshes)
            if (mesh != null)
                mesh.apply();
    }
}
