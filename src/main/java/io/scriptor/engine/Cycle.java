package io.scriptor.engine;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.Stream;

import io.scriptor.engine.component.Component;

public abstract class Cycle {

    private boolean isInit = false;
    private final Engine engine;
    private final Map<Class<?>, List<Component>> components = new HashMap<>();

    protected Cycle(final Engine engine) {
        this.engine = engine;
    }

    public Engine getEngine() {
        return engine;
    }

    public <T extends Component> T addComponent(final Class<T> type, final Object... args) {
        final var paramtypes = new Class<?>[args.length + 1];
        final var params = new Object[args.length + 1];
        for (int i = 0; i < params.length; ++i) {
            paramtypes[i] = i == 0 ? Cycle.class : args[i - 1].getClass();
            params[i] = i == 0 ? this : args[i - 1];
        }

        final T component;
        try {
            component = type
                    .getConstructor(paramtypes)
                    .newInstance(params);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }

        components
                .computeIfAbsent(type, key -> new Vector<>())
                .add(component);
        return component;
    }

    public boolean hasComponent(final Class<? extends Component> type) {
        return components.containsKey(type) && !components.get(type).isEmpty();
    }

    public <T extends Component> T getComponent(final Class<T> type) {
        return getComponent(type, 0);
    }

    public <T extends Component> T getComponent(final Class<T> type, final int index) {
        if (!components.containsKey(type))
            throw new RuntimeException();
        return type.cast(components.get(type).get(index));
    }

    @SuppressWarnings("unchecked")
    public <T extends Component> T[] getComponents(final Class<T> type) {
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
    public <T extends Component> Stream<T> stream(final Class<T> type) {
        if (!components.containsKey(type))
            return Stream.empty();
        return Stream.class.cast(components.get(type).stream());
    }

    public void onInit() {
        isInit = true;
    }

    public void onStart() {
        if (!isInit)
            onInit();
    }

    public void onUpdate() {
        if (!isInit)
            onInit();
    }

    public void onKey(final int key, final int scancode, final int action, final int mods) {
    }

    public void onStop() {
    }

    public void onDestroy() {
        components.values().forEach(list -> list.forEach(Component::destroy));
    }
}
