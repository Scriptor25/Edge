package io.scriptor.edge;

import io.scriptor.engine.Cycle;
import io.scriptor.engine.Engine;
import io.scriptor.engine.component.Camera;
import io.scriptor.engine.component.Transform;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class MainCamera extends Cycle {

    public MainCamera(final @NotNull Engine engine) {
        super(engine);
    }

    @Override
    protected void onStart() {
        addComponent(Transform.class)
                .setTranslation(new Vector3f(10, 10, 0))
                .rotateY(-45)
                .rotateX(30);

        addComponent(Camera.class)
                .setOrtho(true)
                .setNear(-100.0f)
                .setFar(100.0f)
                .setViewY(4.0f);
    }

    @Override
    protected void onUpdate() {
        final var player     = getEngine().getCycle("player", Player.class);
        final var pTransform = player.getComponent(Transform.class);
        final var transform  = getComponent(Transform.class);

        transform.setTranslation(pTransform.getTranslation());
    }
}
