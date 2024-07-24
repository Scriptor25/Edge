package io.scriptor.engine;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Stream;

public abstract class Cycle {

    private Engine engine;
    private final Map<Class<?>, List<Object>> components = new HashMap<>();

    public void setEngine(final Engine engine) {
        this.engine = engine;
    }

    public Engine getEngine() {
        return engine;
    }

    public <T> T addComponent(final Class<T> type, final Object... args) {
        try {
            final var argtypes = new Class<?>[args.length];
            for (int i = 0; i < args.length; ++i)
                argtypes[i] = args[i].getClass();
            final var inst = type.getConstructor(argtypes).newInstance(args);
            components.computeIfAbsent(type, key -> new Vector<>()).add(inst);
            return inst;
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T getComponent(final Class<T> type) {
        return getComponent(type, 0);
    }

    public <T> T getComponent(final Class<T> type, final int index) {
        if (!components.containsKey(type))
            throw new IllegalStateException();
        return type.cast(components.get(type).get(index));
    }

    @SuppressWarnings("unchecked")
    public <T> T[] getComponents(final Class<T> type) {
        final var arty = (Class<T[]>) type.arrayType();
        if (!components.containsKey(type))
            try {
                return arty.getConstructor(int.class).newInstance(0);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                throw new RuntimeException(e);
            }
        return arty.cast(components.get(type).toArray());
    }

    @SuppressWarnings("unchecked")
    public <T> Stream<T> stream(final Class<T> type) {
        return Stream.class.cast(components.get(type).stream());
    }

    public void onInit() {
    }

    public void onStart() {
    }

    public void onUpdate() {
    }

    public void onKey(final int key, final int scancode, final int action, final int mods) {
    }

    public void onStop() {
    }

    public void onDestroy() {
    }
}
