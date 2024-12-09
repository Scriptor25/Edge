package io.scriptor.engine;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.system.NativeResource;

import java.util.Optional;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window implements IDestructible {

    private final long handle;

    public Window(final Engine engine, final String title, final int width, final int height) {
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
