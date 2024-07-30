package io.scriptor.engine;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_SAMPLES;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwGetKey;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwSetWindowSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.system.MemoryUtil.NULL;

import org.lwjgl.glfw.GLFWErrorCallback;

public class Window {

    private final long handle;

    public Window(final Engine engine, final String title, final int width, final int height) {
        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit())
            throw new IllegalStateException();

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_SAMPLES, 4);
        handle = glfwCreateWindow(width, height, title, NULL, NULL);
        if (handle == NULL)
            throw new IllegalStateException();

        glfwSetKeyCallback(handle, engine::onKey);
        glfwSetWindowSizeCallback(handle, engine::onSize);

        glfwMakeContextCurrent(handle);
        glfwSwapInterval(GLFW_TRUE);
    }

    public void open() {
        glfwSetWindowShouldClose(handle, false);
    }

    public void close() {
        glfwSetWindowShouldClose(handle, true);
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

    public void destroy() {
        glfwFreeCallbacks(handle);
        glfwDestroyWindow(handle);

        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }
}
