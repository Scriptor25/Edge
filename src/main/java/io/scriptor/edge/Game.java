package io.scriptor.edge;

import io.scriptor.engine.Cycle;
import io.scriptor.engine.Engine;
import org.jetbrains.annotations.NotNull;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

public class Game extends Cycle {

    public Game(final @NotNull Engine engine) {
        super(engine);
    }

    @Override
    protected void onStart() {
        getEngine().addCycle("world", World.class);
        getEngine().addCycle("player", Player.class);
        getEngine().addCycle("main-camera", MainCamera.class);
    }

    @Override
    protected void onUpdate() {
        if (getEngine().getKeyRelease(GLFW_KEY_ESCAPE))
            getEngine().stop();
    }
}
