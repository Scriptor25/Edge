package io.scriptor.edge;

import io.scriptor.engine.Cycle;
import io.scriptor.engine.Engine;
import io.scriptor.engine.Engine.AxisInput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.scriptor.engine.Engine.AxisType.*;
import static org.lwjgl.glfw.GLFW.*;

public class Game extends Cycle {

    public Game(final @NotNull Engine engine, final @Nullable Cycle parent) {
        super(engine, parent);
    }

    @Override
    protected void onStart() {
        getEngine().addCycle("world", World.class, null);
        getEngine().addCycle("player", Player.class, null);

        getEngine().addAxis("vertical", new AxisInput[] {
                new AxisInput(KEYBOARD, GLFW_KEY_W, -1, false, 0.0f),
                new AxisInput(KEYBOARD, GLFW_KEY_S, -1, true, 0.0f),

                new AxisInput(KEYBOARD, GLFW_KEY_UP, -1, false, 0.0f),
                new AxisInput(KEYBOARD, GLFW_KEY_DOWN, -1, true, 0.0f),

                new AxisInput(KEYBOARD, GLFW_KEY_I, -1, false, 0.0f),
                new AxisInput(KEYBOARD, GLFW_KEY_K, -1, true, 0.0f),

                new AxisInput(JOY_BUTTON, GLFW_GAMEPAD_BUTTON_DPAD_UP, -1, false, 0.0f),
                new AxisInput(JOY_BUTTON, GLFW_GAMEPAD_BUTTON_DPAD_DOWN, -1, true, 0.0f),

                new AxisInput(JOY_AXIS, GLFW_GAMEPAD_AXIS_LEFT_Y, -1, true, 0.2f)
        }, true);

        getEngine().addAxis("horizontal", new AxisInput[] {
                new AxisInput(KEYBOARD, GLFW_KEY_A, -1, true, 0.0f),
                new AxisInput(KEYBOARD, GLFW_KEY_D, -1, false, 0.0f),

                new AxisInput(KEYBOARD, GLFW_KEY_LEFT, -1, true, 0.0f),
                new AxisInput(KEYBOARD, GLFW_KEY_RIGHT, -1, false, 0.0f),

                new AxisInput(KEYBOARD, GLFW_KEY_J, -1, true, 0.0f),
                new AxisInput(KEYBOARD, GLFW_KEY_L, -1, false, 0.0f),

                new AxisInput(JOY_BUTTON, GLFW_GAMEPAD_BUTTON_DPAD_LEFT, -1, true, 0.0f),
                new AxisInput(JOY_BUTTON, GLFW_GAMEPAD_BUTTON_DPAD_RIGHT, -1, false, 0.0f),

                new AxisInput(JOY_AXIS, GLFW_GAMEPAD_AXIS_LEFT_X, -1, false, 0.2f)
        }, true);
    }

    @Override
    protected void onUpdate() {
        if (getEngine().getKeyRelease(GLFW_KEY_ESCAPE))
            getEngine().stop();
    }
}
