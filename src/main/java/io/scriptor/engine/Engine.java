package io.scriptor.engine;

import io.scriptor.engine.component.Camera;
import io.scriptor.engine.component.Model;
import io.scriptor.engine.component.Transform;
import io.scriptor.engine.data.Vertex;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL43.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Engine implements IDestructible {

    private static class Key {

        private boolean now;
        private boolean previous;

        public void update(final boolean now) {
            this.previous = this.now;
            this.now = now;
        }

        public boolean down() {
            return this.now;
        }

        public boolean press() {
            return !this.previous && this.now;
        }

        public boolean release() {
            return this.previous && !this.now;
        }
    }

    private static class Mouse {

        private float xNow, yNow;
        private float xPrevious, yPrevious;

        public void update(final float xNow, final float yNow) {
            this.xPrevious = this.xNow;
            this.yPrevious = this.yNow;
            this.xNow = xNow;
            this.yNow = yNow;
        }

        public float positionX() {
            return this.xNow;
        }

        public float positionY() {
            return this.yNow;
        }

        public float deltaX() {
            return this.xNow - this.xPrevious;
        }

        public float deltaY() {
            return this.yNow - this.yPrevious;
        }
    }

    public enum AxisType {
        KEYBOARD,
        MOUSE_BUTTON,
        MOUSE_AXIS,
        JOY_BUTTON,
        JOY_AXIS,
    }

    public record AxisInput(
            @NotNull AxisType type,
            int index,
            int joystick,
            boolean negative,
            float dead
    ) {
    }

    private static class Axis implements Iterable<AxisInput> {

        private final AxisInput[] inputs;
        private final boolean snap;
        private float value;

        public Axis(final @NotNull AxisInput @NotNull [] inputs, final boolean snap) {
            this.inputs = inputs;
            this.snap = snap;
        }

        public int getInputCount() {
            return inputs.length;
        }

        public @NotNull AxisInput getInput(final int index) {
            return inputs[index];
        }

        public float getValue() {
            return value;
        }

        public void setValue(final float value) {
            this.value = value;
        }

        @Override
        public @NotNull Iterator<AxisInput> iterator() {
            return new Iterator<>() {

                private int index = 0;

                @Override
                public boolean hasNext() {
                    return index < inputs.length;
                }

                @Override
                public AxisInput next() {
                    if (index >= inputs.length)
                        throw new NoSuchElementException();
                    return inputs[index++];
                }
            };
        }
    }

    private final Window window;

    private final Map<String, Cycle> cycles = new HashMap<>();
    private final Map<Integer, Key> keys = new HashMap<>();
    private final Map<Integer, Key> buttons = new HashMap<>();
    private final Mouse mouse = new Mouse();
    private final Map<String, Axis> axes = new HashMap<>();

    private final Map<String, Cycle> nextCycles = new HashMap<>();

    private int width;
    private int height;

    private float previousTime;
    private float deltaTime;

    private Timer timer;

    public Engine(final @NotNull String title, final int width, final int height) {
        window = new Window(this, title, width, height);
        this.width = width;
        this.height = height;

        GL.createCapabilities();

        glEnable(GL_DEBUG_OUTPUT);
        glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS);
        glDebugMessageCallback(this::onMessage, NULL);

        glEnable(GL_MULTISAMPLE);

        for (int key = GLFW_KEY_SPACE; key <= GLFW_KEY_LAST; ++key)
            keys.put(key, new Key());

        for (int button = GLFW_MOUSE_BUTTON_1; button <= GLFW_MOUSE_BUTTON_LAST; ++button)
            buttons.put(button, new Key());
    }

    public <T extends Cycle> @NotNull T addCycle(
            final @NotNull String id,
            final @NotNull Class<T> type,
            final @Nullable Cycle parent,
            final @NotNull Object @NotNull ... args
    ) {
        final var paramTypes = new Class<?>[args.length + 2];
        final var params     = new Object[args.length + 2];
        for (int i = 0; i < params.length; ++i) {
            switch (i) {
                case 0 -> {
                    paramTypes[i] = Engine.class;
                    params[i] = this;
                }

                case 1 -> {
                    paramTypes[i] = Cycle.class;
                    params[i] = parent;
                }

                default -> {
                    paramTypes[i] = args[i - 2].getClass();
                    params[i] = args[i - 2];
                }
            }
        }

        final T cycle;
        try {
            cycle = type
                    .getConstructor(paramTypes)
                    .newInstance(params);
        } catch (final InstantiationException
                       | IllegalAccessException
                       | IllegalArgumentException
                       | InvocationTargetException
                       | NoSuchMethodException
                       | SecurityException e) {
            throw new IllegalStateException(e);
        }

        nextCycles.put(id, cycle);
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

    public boolean getMouseButton(final int button) {
        if (!buttons.containsKey(button))
            return false;
        return buttons.get(button).down();
    }

    public boolean getMouseButtonPress(final int button) {
        if (!buttons.containsKey(button))
            return false;
        return buttons.get(button).press();
    }

    public boolean getMouseButtonRelease(final int button) {
        if (!buttons.containsKey(button))
            return false;
        return buttons.get(button).release();
    }

    public float getMouseAxis(final int axis) {
        return switch (axis) {
            case 0 -> mouse.deltaX();
            case 1 -> mouse.deltaY();
            default -> 0.0f;
        };
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

    public void addAxis(final @NotNull String id, final @NotNull AxisInput @NotNull [] inputs, final boolean snap) {
        if (axes.containsKey(id))
            throw new IllegalStateException();
        axes.put(id, new Axis(inputs, snap));
    }

    public float getAxis(final @NotNull String id) {
        if (!axes.containsKey(id))
            throw new IllegalStateException();
        return axes.get(id).getValue();
    }

    public void start() {
        onStart();

        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                cycles.values().forEach(Cycle::fixed);
            }

        }, 50, 50);

        window.open(true);
        while (window.update())
            onUpdate();

        timer.cancel();

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

        final var aspect = (float) width / (float) height;

        cycles.values()
              .stream()
              .filter(cycle -> cycle.hasComponent(Camera.class))
              .map(cycle -> cycle.getComponent(Camera.class))
              .forEach(camera -> camera.setAspect(aspect));
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

    private void startNextCycles() {
        while (!nextCycles.isEmpty()) {
            cycles.putAll(nextCycles);
            nextCycles.clear();
            cycles.values().forEach(Cycle::start);
        }
    }

    private void onStart() {
        startNextCycles();

        final var aspect = (float) width / (float) height;
        cycles.values()
              .stream()
              .filter(cycle -> cycle.hasComponent(Camera.class))
              .map(cycle -> cycle.getComponent(Camera.class))
              .forEach(camera -> camera.setAspect(aspect));

        timer = new Timer();
    }

    private void updateKeys() {
        keys.forEach((id, key) -> key.update(window.getKey(id)));
    }

    private void updateButtons() {
        buttons.forEach((id, button) -> button.update(window.getMouseButton(id)));
    }

    private void updateMouse() {
        final var position = window.getMousePosition();
        mouse.update(position[0], position[1]);
    }

    private void updateAxes() {
        axes.forEach((id, axis) -> {
            final List<Float> positiveValues = new ArrayList<>();
            final List<Float> negativeValues = new ArrayList<>();
            axis.forEach(input -> {
                final boolean negative;
                final float   value;

                switch (input.type()) {
                    case KEYBOARD:
                        value = getKey(input.index()) ? 1.0f : 0.0f;
                        negative = input.negative();
                        break;
                    case MOUSE_BUTTON:
                        value = getMouseButton(input.index()) ? 1.0f : 0.0f;
                        negative = input.negative();
                        break;
                    case MOUSE_AXIS:
                        value = input.negative() ? -getMouseAxis(input.index()) : getMouseAxis(input.index());
                        negative = value < 0.0f;
                        break;
                    case JOY_BUTTON:
                        value = window.getJoyButton(input.joystick(), input.index()) ? 1.0f : 0.0f;
                        negative = input.negative();
                        break;
                    case JOY_AXIS:
                        value = input.negative()
                                ? -window.getJoyAxis(input.joystick(), input.index())
                                : window.getJoyAxis(input.joystick(), input.index());
                        negative = value < 0.0f;
                        break;
                    default:
                        return;
                }

                final var absValue = Math.abs(value);
                if (absValue <= Math.abs(input.dead()))
                    return;

                if (negative) {
                    negativeValues.add(absValue);
                } else {
                    positiveValues.add(absValue);
                }
            });

            if (!positiveValues.isEmpty() && !negativeValues.isEmpty() && axis.snap)
                axis.setValue(0.0f);
            else {
                final var maxPositive = positiveValues.stream().max(Float::compare).orElse(0.0f);
                final var maxNegative = negativeValues.stream().max(Float::compare).orElse(0.0f);
                axis.setValue(maxPositive - maxNegative);
            }
        });
    }

    private void onUpdate() {
        final var time = getTime();
        deltaTime = time - previousTime;
        previousTime = time;

        startNextCycles();

        updateKeys();
        updateButtons();
        updateMouse();
        updateAxes();

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glEnable(GL_MULTISAMPLE);
        glEnable(GL_BLEND);

        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        final var camera = cycles
                .values()
                .stream()
                .filter(cycle -> cycle.hasComponent(Camera.class))
                .findFirst();
        if (camera.isEmpty()) {
            cycles.values().forEach(Cycle::update);
            return;
        }

        final var cameraTransform = camera.get().getComponent(Transform.class);
        final var cameraCamera    = camera.get().getComponent(Camera.class);

        final var view = cameraTransform.getInverse();
        final var proj = cameraCamera.getMatrix();

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
