package io.scriptor.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class Ref<T extends IDestructible> implements IDestructible {

    private static final Map<Class<?>, Map<String, Ref<?>>> refs = new HashMap<>();

    @SuppressWarnings("unchecked")
    public static <T extends IDestructible> Ref<T> get(final Class<T> type, final String id) {
        return (Ref<T>) refs
                .computeIfAbsent(type, typeKey -> new HashMap<>())
                .computeIfAbsent(id, idKey -> new Ref<>(type, id));
    }

    public static <T extends IDestructible> Ref<T> create(final Class<T> type, final String id, final T t) {
        return Ref.get(type, id).set(t);
    }

    private final Class<T> type;
    private final String id;
    private int numUses = 0;

    private T t = null;
    private boolean ok = false;

    private Ref(final Class<T> type, final String id) {
        this.type = type;
        this.id = id;
    }

    public boolean ok() {
        return ok;
    }

    public T get() {
        if (!this.ok) throw new IllegalStateException();
        return t;
    }

    public Ref<T> set(final T t) {
        if (this.ok) throw new IllegalStateException();
        this.t = t;
        this.ok = true;
        return this;
    }

    public void ok(final Consumer<T> observer) {
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
