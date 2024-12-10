package io.scriptor.edge;

import io.scriptor.engine.Cycle;
import io.scriptor.engine.Engine;
import io.scriptor.engine.component.Camera;
import io.scriptor.engine.component.Transform;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class Player extends Cycle {

    public Player(final Engine engine) {
        super(engine);
    }

    @Override
    public void onStart() {
        super.onStart();

        addComponent(Transform.class)
                .setTranslation(new Vector3f(0, 0, 0))
                .rotateY(-45)
                .rotateX(30)
        ;
        addComponent(Camera.class)
                .setOrtho(true)
                .setNear(-100.0f)
                .setFar(100.0f)
                .setViewY(4.0f);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        final var world = getEngine().getCycle("world", World.class);
        final var bounds = world.getBounds();

        final var transform = getComponent(Transform.class);
        if (getEngine().getKey(GLFW_KEY_UP)) {
            transform.translate(-1, 0, 0);
        }
        if (getEngine().getKey(GLFW_KEY_DOWN)) {
            transform.translate(1, 0, 0);
        }
        if (getEngine().getKey(GLFW_KEY_LEFT)) {
            transform.translate(0, 0, -1);
        }
        if (getEngine().getKey(GLFW_KEY_RIGHT)) {
            transform.translate(0, 0, 1);
        }

        transform.clampTranslation(new Vector3f(0, 0, 0), bounds);
    }
}
