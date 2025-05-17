package io.scriptor.engine.data;

import io.scriptor.engine.IDestructible;
import io.scriptor.engine.Ref;
import io.scriptor.engine.gl.GLBuffer;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4fc;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL15.*;

public class Mesh implements IDestructible {

    public static @NotNull Ref<Mesh> get(final @NotNull String id) {
        return Ref.get(Mesh.class, id);
    }

    public static @NotNull Ref<Mesh> create(final @NotNull String id) {
        return Ref.create(Mesh.class, id, new Mesh());
    }

    private final @NotNull List<Vertex> vertices = new ArrayList<>();
    private final @NotNull List<Integer> indices = new ArrayList<>();

    private final @NotNull GLBuffer vbo = new GLBuffer(GL_ARRAY_BUFFER, GL_DYNAMIC_DRAW);
    private final @NotNull GLBuffer ibo = new GLBuffer(GL_ELEMENT_ARRAY_BUFFER, GL_DYNAMIC_DRAW);

    private Mesh() {
    }

    public void clear() {
        vertices.clear();
        indices.clear();
    }

    public void add(final @NotNull Vertex vertex) {
        vertices.add(vertex);
    }

    public void add(final @NotNull Vertex... vertices) {
        for (final var vertex : vertices)
            add(vertex);
    }

    public void add(final int index) {
        indices.add(index);
    }

    public void add(final int... indices) {
        for (final var index : indices)
            add(index);
    }

    public void addQuad(
            final @NotNull Vector3fc origin,
            final @NotNull Vector3fc edge0,
            final @NotNull Vector3fc edge1,
            final @NotNull Vector4fc color
    ) {
        final var first = vertices.size();

        final var ne0    = edge0.normalize(new Vector3f());
        final var ne1    = edge1.normalize(new Vector3f());
        final var normal = ne0.cross(ne1, new Vector3f());

        add(new Vertex(origin, new Vector2f(0, 0), normal, color));
        add(new Vertex(origin.add(edge0, new Vector3f()), new Vector2f(0, 1), normal, color));
        add(new Vertex(origin.add(edge0, new Vector3f()).add(edge1), new Vector2f(1, 1), normal, color));
        add(new Vertex(origin.add(edge1, new Vector3f()), new Vector2f(1, 0), normal, color));

        add(first, first + 1, first + 2, first + 2, first + 3, first);
    }

    public void apply() {
        final var vb = ByteBuffer
                .allocateDirect(Vertex.BYTES * vertices.size())
                .order(ByteOrder.nativeOrder());
        vertices.forEach(vertex -> vertex.get(vb));
        vb.flip();
        vbo.bind()
           .data(vb)
           .unbind();

        final var ib = ByteBuffer
                .allocateDirect(Integer.BYTES * indices.size())
                .order(ByteOrder.nativeOrder());
        indices.forEach(ib::putInt);
        ib.flip();
        ibo.bind()
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

    @Override
    public void destroy() {
        ibo.destroy();
        vbo.destroy();
    }
}
