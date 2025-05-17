package io.scriptor.engine;

import io.scriptor.engine.component.Camera;
import io.scriptor.engine.component.Model;
import io.scriptor.engine.component.Transform;
import io.scriptor.engine.data.Vertex;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL43.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Engine implements IDestructible {

    private static class KeyRecord {

        private boolean now;
        private boolean previous;

        public void update(final boolean now) {
            this.previous = this.now;
            this.now = now;
        }

        public boolean down() {
            return now;
        }

        public boolean press() {
            return !previous && now;
        }

        public boolean release() {
            return previous && !now;
        }
    }

    private final @NotNull Window window;

    private final @NotNull Queue<Runnable> tasks = new ArrayDeque<>();
    private final @NotNull Map<String, Cycle> cycles = new HashMap<>();
    private final @NotNull Map<Integer, KeyRecord> keys = new HashMap<>();

    private int width;
    private int height;
    private float previous;
    private float deltaTime;

    public Engine(final @NotNull String title, final int width, final int height) {
        window = new Window(this, title, width, height);
        this.width = width;
        this.height = height;

        GL.createCapabilities();

        glEnable(GL_DEBUG_OUTPUT);
        glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS);
        glDebugMessageCallback(this::onMessage, NULL);

        glEnable(GL_MULTISAMPLE);

        for (int key = GLFW_KEY_SPACE; key < GLFW_KEY_LAST; ++key)
            keys.put(key, new KeyRecord());
    }

    public void schedule(final @NotNull Runnable task) {
        tasks.add(task);
    }

    public <T extends Cycle> @NotNull T addCycle(
            final @NotNull String id,
            final @NotNull Class<T> type,
            final @NotNull Object... args
    ) {
        final var paramTypes = new Class<?>[args.length + 1];
        final var params     = new Object[args.length + 1];
        for (int i = 0; i < params.length; ++i) {
            paramTypes[i] = i == 0 ? Engine.class : args[i - 1].getClass();
            params[i] = i == 0 ? this : args[i - 1];
        }

        final T cycle;
        try {
            cycle = type
                    .getConstructor(paramTypes)
                    .newInstance(params);
        } catch (final @NotNull InstantiationException
                                | IllegalAccessException
                                | IllegalArgumentException
                                | InvocationTargetException
                                | NoSuchMethodException
                                | SecurityException e) {
            throw new IllegalStateException(e);
        }

        schedule(() -> cycles.put(id, cycle));
        return cycle;
    }

    public <T extends Cycle> @NotNull T getCycle(final @NotNull String id, final @NotNull Class<T> type) {
        if (!cycles.containsKey(id))
            throw new IllegalStateException();
        return type.cast(cycles.get(id));
    }

    public boolean getKey(final int key) {
        if (!keys.containsKey(key))
            return false;
        return keys.get(key).down();
    }

    public boolean getKeyPress(final int key) {
        if (!keys.containsKey(key))
            return false;
        return keys.get(key).press();
    }

    public boolean getKeyRelease(final int key) {
        if (!keys.containsKey(key))
            return false;
        return keys.get(key).release();
    }

    public float getTime() {
        return (float) glfwGetTime();
    }

    public float getDeltaTime() {
        return deltaTime;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void start() {
        onStart();

        window.open(true);
        while (window.update())
            onUpdate();

        onStop();
    }

    public void stop() {
        window.open(false);
    }

    @Override
    public void destroy() {
        stop();
        while (window.isOpen())
            Thread.onSpinWait();
        onDestroy();
    }

    public void onKey(final long handle, final int key, final int scancode, final int action, final int mods) {
        cycles.forEach((id, cycle) -> cycle.key(key, scancode, action, mods));
    }

    public void onSize(final long handle, final int width, final int height) {
        glViewport(0, 0, width, height);
        this.width = width;
        this.height = height;
    }

    public void onMessage(
            final int source,
            final int type,
            final int id,
            final int severity,
            final int length,
            final long message,
            final long userParam
    ) {
        final var msg = MemoryUtil.memASCII(message);
        System.err.printf("[OpenGL] %s%n", msg);
    }

    private void runTasks() {
        while (!tasks.isEmpty())
            tasks.poll().run();
    }

    private void onStart() {
        runTasks();
    }

    private void onUpdate() {
        final var time = getTime();
        deltaTime = time - previous;
        previous = time;

        runTasks();

        for (final var entry : keys.entrySet()) {
            entry.getValue().update(window.getKey(entry.getKey()));
        }

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glEnable(GL_MULTISAMPLE);
        glEnable(GL_BLEND);

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glClearColor(0.2f, 0.3f, 1.0f, 1.0f);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        final var cam = cycles
                .values()
                .stream()
                .filter(cycle -> cycle.hasComponent(Camera.class))
                .findFirst();
        if (cam.isEmpty()) {
            cycles.values().forEach(Cycle::update);
            return;
        }

        final var cTransform = cam.get().getComponent(Transform.class);
        final var cCamera    = cam.get().getComponent(Camera.class);

        final var view = cTransform.getInverse();
        final var proj = cCamera.getMatrix();

        cycles.forEach((id, cycle) -> cycle.stream(Model.class).forEach(model -> {
            final var transform = cycle.hasComponent(Transform.class)
                                  ? cycle.getComponent(Transform.class).getMatrix()
                                  : new Matrix4f();

            model.getMaterial().ok(material -> {
                material.bind();
                material.getProgram().ok(program -> program
                        .uniform("VIEW", loc -> glUniformMatrix4fv(loc, false, view.get(new float[16])))
                        .uniform("PROJ", loc -> glUniformMatrix4fv(loc, false, proj.get(new float[16])))
                        .uniform("TRANSFORM", loc -> glUniformMatrix4fv(loc, false, transform.get(new float[16])))
                        .uniform("TIME", loc -> glUniform1f(loc, (float) glfwGetTime()))
                        .uniform("SUN_DIRECTION", loc -> glUniform3f(loc, -0.4f, -0.7f, 0.5f)));

                model.stream().filter(Ref::ok).map(Ref::get).forEach(mesh -> {
                    mesh.bind();

                    glEnableVertexAttribArray(0);
                    glEnableVertexAttribArray(1);
                    glEnableVertexAttribArray(2);
                    glEnableVertexAttribArray(3);

                    var offset = NULL;
                    glVertexAttribPointer(0, 3, GL_FLOAT, false, Vertex.BYTES, offset);
                    offset += Float.BYTES * 3L;
                    glVertexAttribPointer(1, 2, GL_FLOAT, false, Vertex.BYTES, offset);
                    offset += Float.BYTES * 2L;
                    glVertexAttribPointer(2, 3, GL_FLOAT, false, Vertex.BYTES, offset);
                    offset += Float.BYTES * 3L;
                    glVertexAttribPointer(3, 4, GL_FLOAT, false, Vertex.BYTES, offset);
                    offset += Float.BYTES * 4L;

                    glDrawElements(GL_TRIANGLES, mesh.count(), GL_UNSIGNED_INT, NULL);

                    glDisableVertexAttribArray(0);
                    glDisableVertexAttribArray(1);
                    glDisableVertexAttribArray(2);
                    glDisableVertexAttribArray(3);

                    mesh.unbind();
                });

                material.unbind();
            });
        }));

        cycles.values().forEach(Cycle::update);
    }

    private void onStop() {
        cycles.values().forEach(Cycle::stop);
    }

    private void onDestroy() {
        cycles.values().forEach(Cycle::destroy);

        GL.destroy();
        window.destroy();
    }
}
