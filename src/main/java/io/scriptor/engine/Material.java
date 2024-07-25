package io.scriptor.engine;

import java.util.HashMap;
import java.util.Map;

public class Material {

    private static final Map<String, Material> instances = new HashMap<>();

    public static Material get(final String id) {
        if (!instances.containsKey(id))
            throw new IllegalStateException(id);
        return instances.get(id);
    }

    public static Material create(final String id, final GLProgram program) {
        if (instances.containsKey(id))
            throw new IllegalStateException(id);
        final var instance = new Material(program);
        instances.put(id, instance);
        return instance;
    }

    public static Material create(final String id, final String programId) {
        return create(id, GLProgram.get(programId));
    }

    private final GLProgram program;
    private int uses = 0;

    private Material(final GLProgram program) {
        this.program = program;
    }

    public void use() {
        ++uses;
    }

    public void drop() {
        --uses;
    }

    public void bind() {
        program.bind();
    }

    public void unbind() {
        program.unbind();
    }

    public GLProgram getProgram() {
        return program;
    }

    public void destroy() {
        if (uses == 0)
            program.destroy();
    }
}
