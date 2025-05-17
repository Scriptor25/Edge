package io.scriptor.engine.data;

import io.scriptor.engine.IDestructible;
import io.scriptor.engine.Ref;
import io.scriptor.engine.gl.GLProgram;
import org.jetbrains.annotations.NotNull;

public class Material implements IDestructible {

    public static @NotNull Ref<Material> get(final @NotNull String id) {
        return Ref.get(Material.class, id);
    }

    public static @NotNull Ref<Material> create(final @NotNull String id, final @NotNull Ref<GLProgram> program) {
        return Ref.create(Material.class, id, new Material(program));
    }

    public static @NotNull Ref<Material> create(final @NotNull String id, final @NotNull String programId) {
        return create(id, GLProgram.get(programId));
    }

    private final @NotNull Ref<GLProgram> program;

    private Material(final @NotNull Ref<GLProgram> program) {
        this.program = program;
        this.program.use();
    }

    public void bind() {
        program.ok(GLProgram::bind);
    }

    public void unbind() {
        program.ok(GLProgram::unbind);
    }

    public @NotNull Ref<GLProgram> getProgram() {
        return program;
    }

    @Override
    public void destroy() {
        program.drop();
    }
}
