package io.scriptor.engine.data;

import io.scriptor.engine.IDestructible;
import io.scriptor.engine.Ref;
import io.scriptor.engine.gl.GLProgram;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

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
    private final @NotNull Map<String, IUniform> uniforms = new HashMap<>();

    private Material(final @NotNull Ref<GLProgram> program) {
        this.program = program;
        this.program.use();
    }

    public void bind() {
        program.ok(GLProgram::bind);
        uniforms.forEach((name, uniform) -> program.ok(x -> x.uniform(name, uniform)));
    }

    public <T extends IUniform> @NotNull T uniform(final @NotNull String name, final @NotNull Class<T> type) {
        if (uniforms.containsKey(name))
            return type.cast(uniforms.get(name));

        final T instance;
        try {
            instance = type.getConstructor().newInstance();
        } catch (final InstantiationException |
                       IllegalAccessException |
                       InvocationTargetException |
                       NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        uniforms.put(name, instance);
        return instance;
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
