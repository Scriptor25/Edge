package io.scriptor.engine;

import io.scriptor.engine.component.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public abstract class Cycle implements IDestructible {

    private boolean started = false;

    private final @NotNull Engine engine;
    private final @Nullable Cycle parent;
    private final @NotNull Map<Class<?>, @NotNull List<Component>> components = new HashMap<>();

    protected Cycle(final @NotNull Engine engine, final @Nullable Cycle parent) {
        this.engine = engine;
        this.parent = parent;
    }

    public @NotNull Engine getEngine() {
        return engine;
    }

    public @Nullable Cycle getParent() {
        return parent;
    }

    public boolean isOrphan() {
        return parent == null;
    }

    public <T extends Component> @NotNull T addComponent(
            final @NotNull Class<T> type,
            final @NotNull Object @NotNull ... args
    ) {
        final var paramTypes = new Class<?>[args.length + 1];
        final var params     = new Object[args.length + 1];
        for (int i = 0; i < params.length; ++i) {
            paramTypes[i] = i == 0 ? Cycle.class : args[i - 1].getClass();
            params[i] = i == 0 ? this : args[i - 1];
        }

        final T component;
        try {
            component = type
                    .getConstructor(paramTypes)
                    .newInstance(params);
        } catch (final @NotNull InstantiationException
                                | IllegalAccessException
                                | IllegalArgumentException
                                | InvocationTargetException
                                | NoSuchMethodException
                                | SecurityException e) {
            throw new IllegalStateException(e);
        }

        components
                .computeIfAbsent(type, key -> new ArrayList<>())
                .add(component);
        return component;
    }

    public boolean hasComponent(final @NotNull Class<? extends Component> type) {
        return components.containsKey(type) && !components.get(type).isEmpty();
    }

    public <T extends Component> @NotNull T getComponent(final @NotNull Class<T> type) {
        return getComponent(type, 0);
    }

    public <T extends Component> @NotNull T getComponent(final @NotNull Class<T> type, final int index) {
        if (!components.containsKey(type))
            throw new IllegalStateException();
        return type.cast(components.get(type).get(index));
    }

    @SuppressWarnings("unchecked")
    public <T extends Component> @NotNull T @NotNull [] getComponents(final @NotNull Class<T> type) {
        final var arty = (Class<T[]>) type.arrayType();
        if (!components.containsKey(type))
            try {
                return arty.getConstructor(int.class).newInstance(0);
            } catch (final @NotNull InstantiationException
                                    | IllegalAccessException
                                    | IllegalArgumentException
                                    | InvocationTargetException
                                    | NoSuchMethodException
                                    | SecurityException e) {
                throw new IllegalStateException(e);
            }
        return arty.cast(components.get(type).toArray());
    }

    @SuppressWarnings("unchecked")
    public <T extends Component> @NotNull Stream<T> stream(final @NotNull Class<T> type) {
        if (!components.containsKey(type))
            return Stream.empty();
        return (Stream<T>) components.get(type).stream();
    }

    public void start() {
        if (started)
            return;

        started = true;
        onStart();
    }

    public void update() {
        if (!started)
            start();

        onUpdate();
    }

    public void fixed() {
        onFixed();
    }

    public void key(final int key, final int scancode, final int action, final int mods) {
        onKey(key, scancode, action, mods);
    }

    public void stop() {
        if (!started)
            return;

        started = false;
        onStop();
    }

    @Override
    public void destroy() {
        onDestroy();
    }

    protected void onStart() {
    }

    protected void onUpdate() {
    }

    protected void onFixed() {
    }

    protected void onKey(final int key, final int scancode, final int action, final int mods) {
    }

    protected void onStop() {
    }

    protected void onDestroy() {
        components.values().forEach(list -> list.forEach(Component::destroy));
    }
}
