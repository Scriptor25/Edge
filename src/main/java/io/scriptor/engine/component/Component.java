package io.scriptor.engine.component;

import io.scriptor.engine.Cycle;
import io.scriptor.engine.Engine;
import io.scriptor.engine.IDestructible;
import org.jetbrains.annotations.NotNull;

public abstract class Component implements IDestructible {

    private final @NotNull Engine engine;
    private final @NotNull Cycle cycle;

    protected Component(final @NotNull Cycle cycle) {
        this.engine = cycle.getEngine();
        this.cycle = cycle;
    }

    public @NotNull Engine getEngine() {
        return engine;
    }

    public @NotNull Cycle getCycle() {
        return cycle;
    }

    @Override
    public void destroy() {
    }
}
