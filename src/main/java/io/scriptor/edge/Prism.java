package io.scriptor.edge;

import org.joml.Math;
import org.joml.Vector3i;
import org.joml.Vector3ic;

import io.scriptor.engine.Cycle;
import io.scriptor.engine.Engine;
import io.scriptor.engine.component.Model;
import io.scriptor.engine.component.Transform;

public class Prism extends Cycle {

    private static float map(float x, float xmin, float xmax, float min, float max) {
        return min + (max - min) * (xmax - x) / (xmax - xmin);
    }

    private final Vector3ic position;
    private float prev;

    public Prism(final Engine engine, final Vector3i position) {
        super(engine);
        this.position = position;
    }

    @Override
    public void onInit() {
        super.onInit();

        final var transform = addComponent(Transform.class);
        transform.scale.set(0.4f);

        addComponent(Model.class, "rainbow", "cube");

        prev = getEngine().getTime();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        final var time = getEngine().getTime();
        final var dt = time - prev;
        prev = time;
        final var transform = getComponent(Transform.class);

        final var dy = Math.sin(time) * 0.1f;
        transform.translation.set(position.x(), position.y() + dy, position.z());
        transform.rotation.rotateY(dt);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
