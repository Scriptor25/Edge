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

    private final Engine m_Engine;
    private final long m_Handle;

    public Window(final Engine engine, final String title, final int width, final int height) {
        m_Engine = engine;

        GLFWErrorCallback.createPrint(System.err).set();
        if (!glfwInit())
            throw new IllegalStateException();

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_SAMPLES, 4);
        m_Handle = glfwCreateWindow(width, height, title, NULL, NULL);
        if (m_Handle == NULL)
            throw new IllegalStateException();

        glfwSetKeyCallback(m_Handle, m_Engine::onKey);
        glfwSetWindowSizeCallback(m_Handle, m_Engine::onSize);

        glfwMakeContextCurrent(m_Handle);
        glfwSwapInterval(GLFW_TRUE);
    }

    public void open() {
        glfwSetWindowShouldClose(m_Handle, false);
    }

    public void close() {
        glfwSetWindowShouldClose(m_Handle, true);
    }

    public boolean isOpen() {
        return !glfwWindowShouldClose(m_Handle);
    }

    public boolean getKey(final int key) {
        return glfwGetKey(m_Handle, key) == GLFW_PRESS;
    }

    public boolean update() {
        if (glfwWindowShouldClose(m_Handle))
            return false;
        glfwSwapBuffers(m_Handle);
        glfwPollEvents();
        return true;
    }

    public void destroy() {
        glfwFreeCallbacks(m_Handle);
        glfwDestroyWindow(m_Handle);

        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }
}
