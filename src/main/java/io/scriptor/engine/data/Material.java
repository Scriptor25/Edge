package io.scriptor.engine.data;

import io.scriptor.engine.IDestructible;
import io.scriptor.engine.Ref;
import io.scriptor.engine.gl.GLProgram;

public class Material implements IDestructible {

    public static Ref<Material> get(final String id) {
        return Ref.get(Material.class, id);
    }

    public static Ref<Material> create(final String id, final Ref<GLProgram> program) {
        return Ref.create(Material.class, id, new Material(program));
    }

    public static Ref<Material> create(final String id, final String programId) {
        return create(id, GLProgram.get(programId));
    }

    private final Ref<GLProgram> program;

    private Material(final Ref<GLProgram> program) {
        this.program = program;
        this.program.use();
    }

    public void bind() {
        program.ok(GLProgram::bind);
    }

    public void unbind() {
        program.ok(GLProgram::unbind);
    }

    public Ref<GLProgram> getProgram() {
        return program;
    }

    @Override
    public void destroy() {
        program.drop();
    }
}
