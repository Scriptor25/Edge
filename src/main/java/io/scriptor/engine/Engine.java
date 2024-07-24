package io.scriptor.engine;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_LAST;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryUtil;

import io.scriptor.engine.GLProgram.ShaderInfo;

public class Engine {

    private static class KeyRecord {

        public boolean now, previous;
    }

    private final Window window;

    private final Map<String, GLProgram> programs = new HashMap<>();
    private final Map<String, Mesh> meshes = new HashMap<>();
    private final Map<String, Material> materials = new HashMap<>();

    private final List<Cycle> cycles = new Vector<>();
    private final Map<Integer, KeyRecord> keyMap = new HashMap<>();

    private int width, height;

    public Engine(final String title, int width, int height) {
        window = new Window(this, title, width, height);
        this.width = width;
        this.height = height;

        GL.createCapabilities();

        glEnable(GL_DEBUG_OUTPUT);
        glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS);
        glDebugMessageCallback(this::onMessage, NULL);

        programs.put("default", new GLProgram(
                new ShaderInfo("shaders/default/vertex.glsl", true, GL_VERTEX_SHADER),
                new ShaderInfo("shaders/default/fragment.glsl", true, GL_FRAGMENT_SHADER)));
        programs.put("base", new GLProgram(
                new ShaderInfo("shaders/base/vertex.glsl", true, GL_VERTEX_SHADER),
                new ShaderInfo("shaders/base/fragment.glsl", true, GL_FRAGMENT_SHADER)));
        programs.put("endframe", new GLProgram(
                new ShaderInfo("shaders/endframe/vertex.glsl", true, GL_VERTEX_SHADER),
                new ShaderInfo("shaders/endframe/fragment.glsl", true, GL_FRAGMENT_SHADER)));

        keyMap.clear();
        for (int key = GLFW_KEY_SPACE; key < GLFW_KEY_LAST; ++key)
            keyMap.put(key, new KeyRecord());
    }

    public void register(final Cycle cycle) {
        cycle.setEngine(this);
        cycles.add(cycle);
    }

    public Mesh createMesh(final String id) {
        final var mesh = new Mesh();
        meshes.put(id, mesh);
        return mesh;
    }

    public Material createMaterial(final String id, final String programName) {
        final var material = new Material(programs.get(programName));
        materials.put(id, material);
        return material;
    }

    public boolean getKey(final int key) {
        if (!keyMap.containsKey(key))
            return false;
        return keyMap.get(key).now;
    }

    public boolean getKeyPress(final int key) {
        if (!keyMap.containsKey(key))
            return false;
        return !keyMap.get(key).previous && keyMap.get(key).now;
    }

    public boolean getKeyRelease(final int key) {
        if (!keyMap.containsKey(key))
            return false;
        return keyMap.get(key).previous && !keyMap.get(key).now;
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
        cycles.stream().forEach(cycle -> cycle.onKey(key, scancode, action, mods));
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
        cycles.stream().forEach(Cycle::onInit);
    }

    private void onStart() {
        cycles.stream().forEach(Cycle::onStart);
    }

    private void onUpdate() {
        for (final var entry : keyMap.entrySet()) {
            entry.getValue().previous = entry.getValue().now;
            entry.getValue().now = window.getKey(entry.getKey());
        }

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glEnable(GL_MULTISAMPLE);
        glClearColor(0.2f, 0.3f, 1.0f, 1.0f);

        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        final var aspect = (float) width / (float) height;
        final var view = new Matrix4f().lookAtLH(20, 8, -3, 6, 1, 12, 0, 1, 0);
        // final var proj = new Matrix4f().perspectiveLH(org.joml.Math.toRadians(90),
        // aspect, 0.3f, 100.0f);
        final var proj = new Matrix4f().orthoLH(-aspect * 5.0f, aspect * 5.0f, -5.0f, 5.0f, 0.3f, 100.0f);

        for (final var material : materials.values()) {
            material.bind();
            material.getProgram()
                    .uniform("VIEW", loc -> glUniformMatrix4fv(loc, false, view.get(new float[16])))
                    .uniform("PROJ", loc -> glUniformMatrix4fv(loc, false, proj.get(new float[16])))
                    .uniform("TIME", loc -> glUniform1f(loc, (float) glfwGetTime()))
                    .uniform("SUN_DIRECTION", loc -> glUniform3f(loc, -0.4f, -0.7f, 0.5f));

            material.stream().forEach(model -> {
                final var mesh = model.getMesh();
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
        }

        cycles.stream().forEach(Cycle::onUpdate);
    }

    private void onStop() {
        cycles.stream().forEach(Cycle::onStop);
    }

    private void onDestroy() {
        cycles.stream().forEach(Cycle::onDestroy);

        for (final var program : programs.values())
            program.destroy();

        GL.destroy();
        window.destroy();
    }
}
