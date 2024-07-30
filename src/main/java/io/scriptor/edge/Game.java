package io.scriptor.edge;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

import org.joml.Vector3f;
import org.joml.Vector3ic;

import io.scriptor.engine.Cycle;
import io.scriptor.engine.Engine;
import io.scriptor.engine.GLProgram;
import io.scriptor.engine.Material;
import io.scriptor.engine.Mesh;
import io.scriptor.engine.Vertex;
import io.scriptor.engine.component.Model;

public class Game extends Cycle {

    private static final String DEFAULT = "default";
    private static final String RAINBOW = "rainbow";

    private int prismIndex = 0;

    public Game(final Engine engine) {
        super(engine);
    }

    private void addPrism(final Vector3ic position) {
        getEngine().addCycle("prism" + (prismIndex++), Prism.class, position);
    }

    @Override
    public void onInit() {
        super.onInit();

        GLProgram.create("shader/default.yaml");
        Material.create(DEFAULT, DEFAULT);

        GLProgram.create("shader/base.yaml");
        Material.create("base", "base");

        GLProgram.create("shader/rainbow.yaml");
        Material.create(RAINBOW, RAINBOW);

        final var defaultMesh = Mesh.create(DEFAULT);
        final var baseMesh = Mesh.create("base");
        final var endframeMesh = Mesh.create("endframe");

        final var cube = Mesh.create("cube");
        cube.add(new Vertex(new Vector3f(-0.5f, -0.5f, -0.5f), new Vector3f(0, 0, 1)));// 0
        cube.add(new Vertex(new Vector3f(-0.5f, -0.5f, -0.5f), new Vector3f(0, 1, 0)));// 1
        cube.add(new Vertex(new Vector3f(-0.5f, -0.5f, -0.5f), new Vector3f(1, 0, 0)));// 2
        cube.add(new Vertex(new Vector3f(-0.5f, -0.5f, 0.5f), new Vector3f(0, 0, -1)));// 3
        cube.add(new Vertex(new Vector3f(-0.5f, -0.5f, 0.5f), new Vector3f(0, 1, 0)));// 4
        cube.add(new Vertex(new Vector3f(-0.5f, -0.5f, 0.5f), new Vector3f(1, 0, 0)));// 5
        cube.add(new Vertex(new Vector3f(-0.5f, 0.5f, -0.5f), new Vector3f(0, 0, 1)));// 6
        cube.add(new Vertex(new Vector3f(-0.5f, 0.5f, -0.5f), new Vector3f(0, -1, 0)));// 7
        cube.add(new Vertex(new Vector3f(-0.5f, 0.5f, -0.5f), new Vector3f(1, 0, 0)));// 8
        cube.add(new Vertex(new Vector3f(-0.5f, 0.5f, 0.5f), new Vector3f(0, 0, -1)));// 9
        cube.add(new Vertex(new Vector3f(-0.5f, 0.5f, 0.5f), new Vector3f(0, -1, 0)));// 10
        cube.add(new Vertex(new Vector3f(-0.5f, 0.5f, 0.5f), new Vector3f(1, 0, 0)));// 11
        cube.add(new Vertex(new Vector3f(0.5f, -0.5f, -0.5f), new Vector3f(0, 0, 1)));// 12
        cube.add(new Vertex(new Vector3f(0.5f, -0.5f, -0.5f), new Vector3f(0, 1, 0)));// 13
        cube.add(new Vertex(new Vector3f(0.5f, -0.5f, -0.5f), new Vector3f(-1, 0, 0)));// 14
        cube.add(new Vertex(new Vector3f(0.5f, -0.5f, 0.5f), new Vector3f(0, 0, -1)));// 15
        cube.add(new Vertex(new Vector3f(0.5f, -0.5f, 0.5f), new Vector3f(0, 1, 0)));// 16
        cube.add(new Vertex(new Vector3f(0.5f, -0.5f, 0.5f), new Vector3f(-1, 0, 0)));// 17
        cube.add(new Vertex(new Vector3f(0.5f, 0.5f, -0.5f), new Vector3f(0, 0, 1)));// 18
        cube.add(new Vertex(new Vector3f(0.5f, 0.5f, -0.5f), new Vector3f(0, -1, 0)));// 19
        cube.add(new Vertex(new Vector3f(0.5f, 0.5f, -0.5f), new Vector3f(-1, 0, 0)));// 20
        cube.add(new Vertex(new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(0, 0, -1)));// 21
        cube.add(new Vertex(new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(0, -1, 0)));// 22
        cube.add(new Vertex(new Vector3f(0.5f, 0.5f, 0.5f), new Vector3f(-1, 0, 0)));// 23
        cube.addQuad(0, 12, 18, 6);
        cube.addQuad(15, 3, 9, 21);
        cube.addQuad(5, 2, 8, 11);
        cube.addQuad(14, 17, 23, 20);
        cube.addQuad(4, 16, 13, 1);
        cube.addQuad(7, 19, 22, 10);
        cube.apply();

        final var world = WorldModel.load(ClassLoader.getSystemResourceAsStream("map/first_contact.yaml"));
        world.generate(
                defaultMesh,
                baseMesh,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                endframeMesh,
                null);

        addComponent(Model.class, DEFAULT, DEFAULT);
        addComponent(Model.class, "base", "base");
        addComponent(Model.class, RAINBOW, "endframe");

        world
                .getPrisms()
                .forEach(this::addPrism);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (getEngine().getKeyRelease(GLFW_KEY_ESCAPE))
            getEngine().stop();
    }
}
