package io.scriptor.edge;

import io.scriptor.engine.IYamlNode;
import io.scriptor.engine.data.Mesh;
import org.joml.*;

import java.io.InputStream;
import java.util.Arrays;
import java.util.stream.Stream;

public class WorldModel {

    private static Vector3ic asVector(final IYamlNode node) {
        final var xyz = node
                .stream()
                .map(e -> e.as(Integer.class).orElseThrow())
                .toArray(Integer[]::new);
        return new Vector3i(xyz[0], xyz[1], xyz[2]);
    }

    public static WorldModel load(final InputStream stream) {
        final var node = IYamlNode.load(stream);

        final var id = node.get("id").as(String.class).orElseThrow();
        final var name = node.get("name").as(String.class).orElseThrow();

        final var bounds = asVector(node.get("bounds"));
        final var spawn = asVector(node.get("spawn"));
        final var prisms = node.get("prisms")
                .stream()
                .map(WorldModel::asVector)
                .toArray(Vector3ic[]::new);

        final var world = new WorldModel(id, name, bounds, spawn, prisms);

        final var voxels = node.get("voxels")
                .stream()
                .map(y -> y
                        .stream()
                        .map(z -> z
                                .stream()
                                .map(x -> x.as(Integer.class).orElseThrow())
                                .map(i -> i < 0 ? null : VoxelType.values()[i])
                                .toArray(VoxelType[]::new))
                        .toArray(VoxelType[][]::new))
                .toArray(VoxelType[][][]::new);

        for (int z = 0; z < bounds.z(); ++z)
            for (int y = 0; y < bounds.y(); ++y)
                for (int x = 0; x < bounds.x(); ++x) {
                    final var type = voxels[y][bounds.z() - z - 1][x];
                    world.set(x, y, z, type);
                }
        return world;
    }

    public enum VoxelType {
        BASE,
        PULSE_NORTH,
        PULSE_SOUTH,
        PULSE_EAST,
        PULSE_WEST,
        SHRINK,
        MAGNIFY,
        PUSH,
        END_FRAME,
        END_CENTER
    }

    private final String id;
    private final String name;
    private final Vector3ic bounds;
    private final Vector3ic spawn;
    private final Vector3ic[] prisms;
    private final VoxelType[][][] voxels;

    public WorldModel(final String id, final String name, final Vector3ic bounds, final Vector3ic spawn,
                      final Vector3ic[] prisms) {
        this.id = id;
        this.name = name;
        this.bounds = bounds;
        this.spawn = spawn;
        this.prisms = prisms;
        this.voxels = new VoxelType[bounds.z()][bounds.y()][bounds.x()];
    }

    public String getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Vector3ic getBounds() {
        return bounds;
    }

    public Vector3ic getSpawn() {
        return spawn;
    }

    public int getPrismCount() {
        return prisms.length;
    }

    public Vector3ic getPrism(final int index) {
        return prisms[index];
    }

    public Stream<Vector3ic> getPrisms() {
        return Arrays.stream(prisms);
    }

    public VoxelType get(final int x, final int y, final int z) {
        if (x < 0 || x >= bounds.x() || y < 0 || y >= bounds.y() || z < 0 || z >= bounds.z())
            return null;
        return voxels[z][y][x];
    }

    public void set(final int x, final int y, final int z, final VoxelType voxel) {
        if (x < 0 || x >= bounds.x() || y < 0 || y >= bounds.y() || z < 0 || z >= bounds.z())
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

        final var baseColor = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);
        final var checkerColor = new Vector4f(0.95f, 0.95f, 0.95f, 1.0f);

        for (int z = 0; z < bounds.z(); ++z)
            for (int y = 0; y < bounds.y(); ++y)
                for (int x = 0; x < bounds.x(); ++x)
                    generateBlock(x, y, z, defaultMesh, meshes, baseColor, checkerColor);

        defaultMesh.apply();
        for (final var mesh : meshes)
            if (mesh != null)
                mesh.apply();
    }

    private void generateBlock(
            final int x,
            final int y,
            final int z,
            final Mesh defaultMesh,
            final Mesh[] meshes,
            final Vector4fc baseColor,
            final Vector4fc checkerColor) {
        final var type = get(x, y, z);
        if (type == null)
            return;

        final var mesh = meshes[type.ordinal()] == null
                ? defaultMesh
                : meshes[type.ordinal()];

        final Vector4fc color;
        final Mesh baseMesh;
        switch (type) {
            case BASE:
                final var isEven = (x + z) % 2 == 0;
                color = isEven ? baseColor : checkerColor;
                baseMesh = mesh;
                break;

            case PULSE_NORTH,
                 PULSE_SOUTH,
                 PULSE_EAST,
                 PULSE_WEST,
                 SHRINK,
                 MAGNIFY,
                 PUSH,
                 END_FRAME,
                 END_CENTER:
            default:
                color = baseColor;
                baseMesh = meshes[0];
                break;
        }

        generateBlock(new Vector3i(x, y, z), baseMesh, mesh, baseColor, color);
    }

    private void generateBlock(
            final Vector3ic pos,
            final Mesh baseMesh,
            final Mesh mesh,
            final Vector4fc baseColor,
            final Vector4fc color) {
        if (pos.y() == 0)
            generateHalfBlock(pos, baseMesh, mesh, baseColor, color);
        else
            generateFullBlock(pos, baseMesh, mesh, baseColor, color);
    }

    private static final Vector3fc DX = new Vector3f(0.5f, 0.0f, 0.0f);
    private static final Vector3fc DY = new Vector3f(0.0f, 0.5f, 0.0f);
    private static final Vector3fc DZ = new Vector3f(0.0f, 0.0f, 0.5f);
    public static final Vector3fc RIGHT = new Vector3f(1.0f, 0.0f, 0.0f);
    public static final Vector3fc UP = new Vector3f(0.0f, 1.0f, 0.0f);
    public static final Vector3fc FORWARD = new Vector3f(0.0f, 0.0f, 1.0f);

    private void generateHalfBlock(
            final Vector3ic pos,
            final Mesh baseMesh,
            final Mesh mesh,
            final Vector4fc baseColor,
            final Vector4fc color) {
        if (isAir(pos.x() - 1, pos.y(), pos.z()))
            baseMesh.addQuad(new Vector3f(pos).sub(DX).sub(DZ), DY, FORWARD, baseColor);
        if (isAir(pos.x() + 1, pos.y(), pos.z()))
            baseMesh.addQuad(new Vector3f(pos).add(DX).sub(DZ), FORWARD, DY, baseColor);
        baseMesh.addQuad(new Vector3f(pos).sub(DX).sub(DZ), FORWARD, RIGHT, baseColor);
        if (isAir(pos.x(), pos.y() + 1, pos.z()))
            mesh.addQuad(new Vector3f(pos).sub(DX).add(DY).sub(DZ), RIGHT, FORWARD, color);
        if (isAir(pos.x(), pos.y(), pos.z() - 1))
            baseMesh.addQuad(new Vector3f(pos).sub(DX).sub(DZ), RIGHT, DY, baseColor);
        if (isAir(pos.x(), pos.y(), pos.z() + 1))
            baseMesh.addQuad(new Vector3f(pos).sub(DX).add(DZ), DY, RIGHT, baseColor);
    }

    private void generateFullBlock(
            final Vector3ic pos,
            final Mesh baseMesh,
            final Mesh mesh,
            final Vector4fc baseColor,
            final Vector4fc color) {
        if (isAir(pos.x() - 1, pos.y(), pos.z()))
            baseMesh.addQuad(new Vector3f(pos).sub(DX).sub(DY).sub(DZ), UP, FORWARD, baseColor);
        if (isAir(pos.x() + 1, pos.y(), pos.z()))
            baseMesh.addQuad(new Vector3f(pos).add(DX).sub(DY).sub(DZ), FORWARD, UP, baseColor);
        if (isAir(pos.x(), pos.y() - 1, pos.z()))
            baseMesh.addQuad(new Vector3f(pos).sub(DX).sub(DY).sub(DZ), FORWARD, RIGHT, baseColor);
        if (isAir(pos.x(), pos.y() + 1, pos.z()))
            mesh.addQuad(new Vector3f(pos).sub(DX).add(DY).sub(DZ), RIGHT, FORWARD, color);
        if (isAir(pos.x(), pos.y(), pos.z() - 1))
            baseMesh.addQuad(new Vector3f(pos).sub(DX).sub(DY).sub(DZ), RIGHT, UP, baseColor);
        if (isAir(pos.x(), pos.y(), pos.z() + 1))
            baseMesh.addQuad(new Vector3f(pos).sub(DX).sub(DY).add(DZ), UP, RIGHT, baseColor);
    }
}
