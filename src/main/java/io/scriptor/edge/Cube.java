package io.scriptor.edge;

import io.scriptor.engine.Cycle;
import io.scriptor.engine.Engine;
import io.scriptor.engine.component.Model;
import io.scriptor.engine.component.Transform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Cube extends Cycle {

    public Cube(final @NotNull Engine engine, final @Nullable Cycle parent) {
        super(engine, parent);
    }

    @Override
    protected void onStart() {
        addComponent(Transform.class);
        addComponent(Model.class, "rainbow", "cube");
    }
}
