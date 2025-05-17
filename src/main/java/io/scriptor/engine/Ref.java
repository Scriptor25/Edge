package io.scriptor.engine;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class Ref<T extends IDestructible> implements IDestructible {

    private static final @NotNull Map<Class<?>, @NotNull Map<String, @NotNull Ref<?>>> refs = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T extends IDestructible> @NotNull Ref<T> get(
            final @NotNull Class<T> type,
            final @NotNull String id
    ) {
        return (Ref<T>) refs
                .computeIfAbsent(type, typeKey -> new HashMap<>())
                .computeIfAbsent(id, idKey -> new Ref<>(type, id));
    }

    public static <T extends IDestructible> @NotNull Ref<T> create(
            final @NotNull Class<T> type,
            final @NotNull String id,
            final @NotNull T t
    ) {
        return Ref.get(type, id).set(t);
    }

    private final @NotNull Class<T> type;
    private final @NotNull String id;
    private int numUses = 0;

    private @Nullable T t = null;
    private boolean ok = false;

    private Ref(final @NotNull Class<T> type, final @NotNull String id) {
        this.type = type;
        this.id = id;
    }

    public boolean ok() {
        return ok;
    }

    public @NotNull T get() {
        if (!this.ok)
            throw new IllegalStateException();
        return Objects.requireNonNull(t);
    }

    public @NotNull Ref<T> set(final @NotNull T t) {
        if (this.ok)
            throw new IllegalStateException();
        this.t = t;
        this.ok = true;
        return this;
    }

    public void ok(final @NotNull Consumer<T> observer) {
        if (this.ok)
            observer.accept(this.t);
    }

    public void use() {
        ++numUses;
    }

    public void drop() {
        if (--numUses == 0)
            destroy();
    }

    @Override
    public void destroy() {
        ok(IDestructible::destroy);
        refs.get(type).remove(id);
    }
}
