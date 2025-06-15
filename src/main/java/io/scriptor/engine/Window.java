package io.scriptor.engine;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWGamepadState;
import org.lwjgl.system.NativeResource;

import java.util.Optional;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window implements IDestructible {

    private final long handle;

    public Window(final @NotNull Engine engine, final @NotNull String title, final int width, final int height) {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit())
            throw new IllegalStateException();

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_SAMPLES, 4);
        glfwWindowHint(GLFW_CONTEXT_DEBUG, GLFW_TRUE);
        handle = glfwCreateWindow(width, height, title, NULL, NULL);
        if (handle == NULL)
            throw new IllegalStateException();

        Optional.ofNullable(glfwSetKeyCallback(handle, engine::onKey)).ifPresent(NativeResource::close);
        Optional.ofNullable(glfwSetWindowSizeCallback(handle, engine::onSize)).ifPresent(NativeResource::close);

        glfwMakeContextCurrent(handle);
        glfwSwapInterval(GLFW_TRUE);
    }

    public void open(final boolean open) {
        glfwSetWindowShouldClose(handle, !open);
    }

    public boolean isOpen() {
        return !glfwWindowShouldClose(handle);
    }

    public boolean getKey(final int key) {
        return glfwGetKey(handle, key) == GLFW_PRESS;
    }

    public boolean getMouseButton(final int button) {
        return glfwGetMouseButton(handle, button) == GLFW_PRESS;
    }

    public float[] getMousePosition() {
        final double[] x = new double[1], y = new double[1];
        glfwGetCursorPos(handle, x, y);
        return new float[] { (float) x[0], (float) y[0] };
    }

    public boolean isPresent(final int joystick) {
        return glfwJoystickPresent(joystick);
    }

    public boolean isGamepad(final int joystick) {
        return glfwJoystickIsGamepad(joystick);
    }

    public int findGamepad() {
        for (int i = GLFW_JOYSTICK_1; i <= GLFW_JOYSTICK_16; ++i)
            if (isPresent(i) && isGamepad(i))
                return i;
        return -1;
    }

    public @Nullable GLFWGamepadState getState(int joystick) {
        if (joystick < 0) {
            final var i = findGamepad();
            if (i < 0)
                return null;

            joystick = i;
        }

        final var state = GLFWGamepadState.create();
        if (glfwGetGamepadState(joystick, state))
            return state;

        return null;
    }

    public boolean getJoyButton(final int joystick, final int button) {
        final var state = getState(joystick);
        if (state == null)
            return false;

        return state.buttons(button) != 0;
    }

    public float getJoyAxis(final int joystick, final int axis) {
        final var state = getState(joystick);
        if (state == null)
            return 0.0f;

        return state.axes(axis);
    }

    public boolean update() {
        if (glfwWindowShouldClose(handle))
            return false;
        glfwSwapBuffers(handle);
        glfwPollEvents();
        return true;
    }

    @Override
    public void destroy() {
        glfwFreeCallbacks(handle);
        glfwDestroyWindow(handle);

        glfwTerminate();
        Optional.ofNullable(glfwSetErrorCallback(null)).ifPresent(NativeResource::close);
    }
}
