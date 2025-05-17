package io.scriptor.edge;

import io.scriptor.engine.Cycle;
import io.scriptor.engine.Engine;
import io.scriptor.engine.component.Model;
import io.scriptor.engine.component.Transform;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class Player extends Cycle {

    private enum Direction {
        NORTH,
        SOUTH,
        WEST,
        EAST,
    }

    public Player(final @NotNull Engine engine) {
        super(engine);
    }

    @Override
    protected void onStart() {
        final var world = getEngine().getCycle("world", World.class);
        final var spawn = world.getSpawn();

        addComponent(Transform.class)
                .setTranslation(spawn);

        addComponent(Model.class, "rainbow", "cube");
    }

    @Override
    protected void onUpdate() {
        if (getEngine().getKey(GLFW_KEY_W)) {
            move(Direction.NORTH);
        }
        if (getEngine().getKey(GLFW_KEY_S)) {
            move(Direction.SOUTH);
        }
        if (getEngine().getKey(GLFW_KEY_A)) {
            move(Direction.WEST);
        }
        if (getEngine().getKey(GLFW_KEY_D)) {
            move(Direction.EAST);
        }
    }

    private void move(final @NotNull Direction direction) {
        final var transform = getComponent(Transform.class);

        final var world  = getEngine().getCycle("world", World.class);
        final var bounds = world.getBounds();

        switch (direction) {
            case NORTH -> transform.translate(-1, 0, 0);
            case SOUTH -> transform.translate(1, 0, 0);
            case WEST -> transform.translate(0, 0, -1);
            case EAST -> transform.translate(0, 0, 1);
        }

        transform.clampTranslation(new Vector3f(0, 0, 0), bounds);
    }
}
