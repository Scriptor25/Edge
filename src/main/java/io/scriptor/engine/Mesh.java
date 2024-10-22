package io.scriptor.engine;

import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

public class Mesh {

    private static final Map<String, Mesh> instances = new HashMap<>();

    public static Mesh get(final String id) {
        if (!instances.containsKey(id))
            throw new IllegalStateException(id);
        return instances.get(id);
    }

    public static Mesh create(final String id) {
        if (instances.containsKey(id))
            throw new IllegalStateException(id);
        final var instance = new Mesh();
        instances.put(id, instance);
        return instance;
    }

    private final List<Vertex> vertices = new ArrayList<>();
    private final List<Integer> indices = new ArrayList<>();

    private final GLBuffer vbo = new GLBuffer(GL_ARRAY_BUFFER, GL_DYNAMIC_DRAW);
    private final GLBuffer ibo = new GLBuffer(GL_ELEMENT_ARRAY_BUFFER, GL_DYNAMIC_DRAW);

    private int uses = 0;

    private Mesh() {
    }

    public void use() {
        ++uses;
    }

    public void drop() {
        --uses;
    }

    public void clear() {
        vertices.clear();
        indices.clear();
    }

    public void add(final Vertex vertex) {
        vertices.add(vertex);
    }

    public void add(final int index) {
        indices.add(index);
    }

    public void add(final int... indices) {
        for (final var index : indices)
            add(index);
    }

    public void addQuad(final int... indices) {
        add(indices[0], indices[1], indices[2], indices[2], indices[3], indices[0]);
    }

    public void addQuad(final Vertex... vertices) {
        final var first = this.vertices.size();
        for (final var vertex : vertices)
            add(vertex);
        add(first, first + 1, first + 2, first + 2, first + 3, first);
    }

    public void addQuad(final Vector3fc origin, final Vector3fc edge0, final Vector3fc edge1, final Vector4fc color) {
        final var first = vertices.size();

        final var ne0 = edge0.normalize(new Vector3f());
        final var ne1 = edge1.normalize(new Vector3f());
        final var normal = ne0.cross(ne1, new Vector3f());

        add(new Vertex(origin, normal, color));
        add(new Vertex(origin.add(edge0, new Vector3f()), normal, color));
        add(new Vertex(origin.add(edge0, new Vector3f()).add(edge1), normal, color));
        add(new Vertex(origin.add(edge1, new Vector3f()), normal, color));

        add(first, first + 1, first + 2, first + 2, first + 3, first);
    }

    public void apply() {
        final var vb = ByteBuffer
                .allocateDirect(Vertex.BYTES * vertices.size())
                .order(ByteOrder.nativeOrder());
        vertices.stream().forEach(vertex -> vertex.get(vb));
        vb.rewind();
        vbo
                .bind()
                .data(vb)
                .unbind();

        final var ib = ByteBuffer
                .allocateDirect(Integer.BYTES * indices.size())
                .order(ByteOrder.nativeOrder());
        indices.stream().forEach(ib::putInt);
        ib.rewind();
        ibo
                .bind()
                .data(ib)
                .unbind();
    }

    public void bind() {
        vbo.bind();
        ibo.bind();
    }

    public void unbind() {
        vbo.unbind();
        ibo.unbind();
    }

    public int count() {
        return indices.size();
    }

    public void destroy() {
        if (uses == 0) {
            vbo.destroy();
            ibo.destroy();
        }
    }
}
