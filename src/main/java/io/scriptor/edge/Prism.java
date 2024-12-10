package io.scriptor.edge;

import io.scriptor.engine.Cycle;
import io.scriptor.engine.Engine;
import io.scriptor.engine.component.Model;
import io.scriptor.engine.component.Transform;
import org.joml.Math;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;

public class Prism extends Cycle {

    private final Vector3fc position;

    public Prism(final Engine engine, final Vector3i position) {
        super(engine);
        this.position = new Vector3f(position);
    }

    @Override
    public void onStart() {
        super.onStart();

        addComponent(Transform.class).setScale(0.4f);
        addComponent(Model.class, "rainbow", "cube");
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        final var dt = getEngine().getDeltaTime() * 2.0f;
        final var transform = getComponent(Transform.class);

        final var dy = Math.sin(getEngine().getTime()) * 0.2f;
        transform
                .setTranslation(position.add(0, dy, 0, new Vector3f()))
                .rotate(dt, new Vector3f(0, 1, 0));
    }
}
