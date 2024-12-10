package io.scriptor.edge;

import io.scriptor.engine.Cycle;
import io.scriptor.engine.Engine;
import io.scriptor.engine.component.Model;
import io.scriptor.engine.component.Transform;
import org.joml.Math;
import org.joml.Vector3i;
import org.joml.Vector3ic;

public class Prism extends Cycle {

    private final Vector3ic position;

    public Prism(final Engine engine, final Vector3i position) {
        super(engine);
        this.position = position;
    }

    @Override
    public void onStart() {
        super.onStart();

        final var transform = addComponent(Transform.class);
        transform.getScale().set(0.4f);

        addComponent(Model.class, "rainbow", "cube");
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        final var dt = getEngine().getDeltaTime() * 2.0f;
        final var transform = getComponent(Transform.class);

        final var dy = Math.sin(getEngine().getTime()) * 0.2f;
        transform.getTranslation().set(position.x(), position.y() + dy, position.z());
        transform.getRotation().rotateY(dt);
    }
}
