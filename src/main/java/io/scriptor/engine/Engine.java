package io.scriptor.engine;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_LAST;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glUniform1f;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL43.GL_DEBUG_OUTPUT;
import static org.lwjgl.opengl.GL43.GL_DEBUG_OUTPUT_SYNCHRONOUS;
import static org.lwjgl.opengl.GL43.glDebugMessageCallback;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;

import io.scriptor.engine.component.Model;
import io.scriptor.engine.component.Transform;

public class Engine {

    private static class KeyRecord {

        public boolean now, previous;
    }

    private final Window window;

    private final List<Runnable> tasks = new Vector<>();
    private final Map<String, Cycle> cycles = new HashMap<>();
    private final Map<Integer, KeyRecord> keys = new HashMap<>();

    private int width, height;

    public Engine(final String title, int width, int height) {
        window = new Window(this, title, width, height);
        this.width = width;
        this.height = height;

        GL.createCapabilities();

        glEnable(GL_DEBUG_OUTPUT);
        glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS);
        glDebugMessageCallback(this::onMessage, NULL);

        for (int key = GLFW_KEY_SPACE; key < GLFW_KEY_LAST; ++key)
            keys.put(key, new KeyRecord());
    }

    public void schedule(final Runnable task) {
        tasks.add(task);
    }

    public <T extends Cycle> T addCycle(final String id, final Class<T> type, final Object... args) {
        final var paramtypes = new Class<?>[args.length + 1];
        final var params = new Object[args.length + 1];
        for (int i = 0; i < params.length; ++i) {
            paramtypes[i] = i == 0 ? Engine.class : args[i - 1].getClass();
            params[i] = i == 0 ? this : args[i - 1];
        }

        final T cycle;
        try {
            cycle = type
                    .getConstructor(paramtypes)
                    .newInstance(params);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }

        schedule(() -> cycles.put(id, cycle));
        return cycle;
    }

    public boolean getKey(final int key) {
        if (!keys.containsKey(key))
            return false;
        return keys.get(key).now;
    }

    public boolean getKeyPress(final int key) {
        if (!keys.containsKey(key))
            return false;
        return !keys.get(key).previous && keys.get(key).now;
    }

    public boolean getKeyRelease(final int key) {
        if (!keys.containsKey(key))
            return false;
        return keys.get(key).previous && !keys.get(key).now;
    }

    public float getTime() {
        return (float) glfwGetTime();
    }

    public void start() {
        onInit();
        onStart();

        window.open();
        while (window.update())
            onUpdate();

        onStop();
    }

    public void stop() {
        window.close();
    }

    public void destroy() {
        stop();
        try {
            while (window.isOpen())
                Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onDestroy();
    }

    public void onKey(final long window, final int key, final int scancode, final int action, final int mods) {
        cycles.forEach((id, cycle) -> cycle.onKey(key, scancode, action, mods));
    }

    public void onSize(final long window, final int width, final int height) {
        glViewport(0, 0, width, height);
        this.width = width;
        this.height = height;
    }

    public void onMessage(int source, int type, int id, int severity, int length, long message, long userParam) {
        final var msg = MemoryUtil.memASCII(message);
        System.err.printf("[OpenGL] %s%n", msg);
        return;
    }

    private void onInit() {
        cycles
                .values()
                .stream()
                .forEach(Cycle::onInit);
    }

    private void onStart() {
        cycles
                .values()
                .stream()
                .forEach(Cycle::onStart);
    }

    private void onUpdate() {
        while (!tasks.isEmpty()) {
            final var task = tasks.get(0);
            tasks.remove(0);
            task.run();
        }

        for (final var entry : keys.entrySet()) {
            entry.getValue().previous = entry.getValue().now;
            entry.getValue().now = window.getKey(entry.getKey());
        }

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glEnable(GL_MULTISAMPLE);
        glEnable(GL_BLEND);

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glClearColor(0.2f, 0.3f, 1.0f, 1.0f);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        final var aspect = (float) width / (float) height;
        final var view = new Matrix4f().lookAtLH(18, 14, 0, 6, 2, 12, 0, 1, 0);
        final var proj = new Matrix4f().orthoLH(-aspect * 10.0f, aspect * 10.0f, -10.0f, 10.0f, 0.3f, 100.0f);

        cycles.forEach((id, cycle) -> cycle.stream(Model.class).forEach(model -> {
            final var transform = cycle.hasComponent(Transform.class)
                    ? cycle.getComponent(Transform.class).getMatrix()
                    : new Matrix4f();

            final var material = model.getMaterial();
            material.bind();
            material.getProgram()
                    .uniform("VIEW", loc -> glUniformMatrix4fv(loc, false, view.get(new float[16])))
                    .uniform("PROJ", loc -> glUniformMatrix4fv(loc, false, proj.get(new float[16])))
                    .uniform("TRANSFORM", loc -> glUniformMatrix4fv(loc, false, transform.get(new float[16])))
                    .uniform("TIME", loc -> glUniform1f(loc, (float) glfwGetTime()))
                    .uniform("SUN_DIRECTION", loc -> glUniform3f(loc, -0.4f, -0.7f, 0.5f));

            model.stream().forEach(mesh -> {
                mesh.bind();

                glEnableVertexAttribArray(0);
                glEnableVertexAttribArray(1);
                glEnableVertexAttribArray(2);

                glVertexAttribPointer(0, 3, GL_FLOAT, false, Vertex.BYTES, 0);
                glVertexAttribPointer(1, 3, GL_FLOAT, false, Vertex.BYTES, Float.BYTES * 3);
                glVertexAttribPointer(2, 4, GL_FLOAT, false, Vertex.BYTES, Float.BYTES * 6);

                glDrawElements(GL_TRIANGLES, mesh.count(), GL_UNSIGNED_INT, NULL);

                glDisableVertexAttribArray(0);
                glDisableVertexAttribArray(1);
                glDisableVertexAttribArray(2);

                mesh.unbind();
            });

            material.unbind();
        }));

        cycles
                .values()
                .stream()
                .forEach(Cycle::onUpdate);
    }

    private void onStop() {
        cycles
                .values()
                .stream()
                .forEach(Cycle::onStop);
    }

    private void onDestroy() {
        cycles
                .values()
                .stream()
                .forEach(Cycle::onDestroy);

        GL.destroy();
        window.destroy();
    }
}
