package io.scriptor.edge;

import io.scriptor.engine.Cycle;
import io.scriptor.engine.Engine;
import io.scriptor.engine.component.Camera;
import io.scriptor.engine.component.Transform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MainCamera extends Cycle {

    public MainCamera(final @NotNull Engine engine, final @Nullable Cycle parent) {
        super(engine, parent);
    }

    @Override
    protected void onStart() {
        addComponent(Transform.class)
                .rotateY(-45)
                .rotateX(30);

        addComponent(Camera.class)
                .setOrtho(true)
                .setNear(-100.0f)
                .setFar(100.0f)
                .setViewY(4.0f);
    }
}
