package io.scriptor.engine.component;

import io.scriptor.engine.Cycle;
import io.scriptor.engine.Engine;
import io.scriptor.engine.IDestructible;

public abstract class Component implements IDestructible {

    private final Engine engine;
    private final Cycle cycle;

    protected Component(final Cycle cycle) {
        this.engine = cycle.getEngine();
        this.cycle = cycle;
    }

    public Engine getEngine() {
        return engine;
    }

    public Cycle getCycle() {
        return cycle;
    }

    @Override
    public void destroy() {
    }
}
