package io.scriptor.edge;

import io.scriptor.engine.Cycle;
import io.scriptor.engine.Engine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

public class Game extends Cycle {

    public Game(final @NotNull Engine engine, final @Nullable Cycle parent) {
        super(engine, parent);
    }

    @Override
    protected void onStart() {
        getEngine().addCycle("world", World.class, null);
        getEngine().addCycle("player", Player.class, null);
    }

    @Override
    protected void onUpdate() {
        if (getEngine().getKeyRelease(GLFW_KEY_ESCAPE))
            getEngine().stop();
    }
}
