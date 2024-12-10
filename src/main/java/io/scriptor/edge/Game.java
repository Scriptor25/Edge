package io.scriptor.edge;

import io.scriptor.engine.Cycle;
import io.scriptor.engine.Engine;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

public class Game extends Cycle {

    public Game(final Engine engine) {
        super(engine);
    }

    @Override
    public void onStart() {
        super.onStart();

        getEngine().addCycle("player", Player.class);
        getEngine().addCycle("world", World.class);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (getEngine().getKeyRelease(GLFW_KEY_ESCAPE))
            getEngine().stop();
    }
}
