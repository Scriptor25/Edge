package io.scriptor.engine.component;

import io.scriptor.engine.Cycle;
import io.scriptor.engine.Engine;

public abstract class Component {

    private final Engine engine;
    private final Cycle cycle;

    public Component(final Cycle cycle) {
        this.engine = cycle.getEngine();
        this.cycle = cycle;
    }

    public Engine getEngine() {
        return engine;
    }

    public Cycle getCycle() {
        return cycle;
    }

    public void destroy() {
    }
}
