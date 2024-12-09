package io.scriptor.edge;

import io.scriptor.engine.Cycle;
import io.scriptor.engine.Engine;
import io.scriptor.engine.ModelLoader;
import io.scriptor.engine.component.Camera;
import io.scriptor.engine.component.Model;
import io.scriptor.engine.data.Material;
import io.scriptor.engine.data.Mesh;
import io.scriptor.engine.data.Resources;
import io.scriptor.engine.gl.GLProgram;
import org.joml.Vector3ic;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

public class Game extends Cycle {

    private static final String DEFAULT = "default";
    private static final String BASE = "base";
    private static final String RAINBOW = "rainbow";
    private static final String END_FRAME = "end_frame";

    private int prismIndex = 0;

    public Game(final Engine engine) {
        super(engine);
    }

    private void addPrism(final Vector3ic position) {
        getEngine().addCycle("prism[" + (prismIndex++) + "]", Prism.class, position);
    }

    @Override
    public void onStart() {
        super.onStart();

        Resources.open("shader/default.yaml", stream -> Material.create(DEFAULT, GLProgram.create(stream)));
        Resources.open("shader/base.yaml", stream -> Material.create(BASE, GLProgram.create(stream)));
        Resources.open("shader/rainbow.yaml", stream -> Material.create(RAINBOW, GLProgram.create(stream)));

        final var defaultMesh = Mesh.create(DEFAULT).get();
        final var baseMesh = Mesh.create(BASE).get();
        final var endFrameMesh = Mesh.create(END_FRAME).get();

        Resources.openVoid("model/cube.yaml", ModelLoader::loadModel);

        Resources
                .open("map/first_contact.yaml", WorldModel::load)
                .ifPresent(world -> {
                    world.generate(defaultMesh, baseMesh, null, null, null, null, null, null, null, endFrameMesh, null);
                    world.getPrisms().forEach(this::addPrism);
                });

        addComponent(Model.class, DEFAULT, DEFAULT);
        addComponent(Model.class, BASE, BASE);
        addComponent(Model.class, RAINBOW, END_FRAME);

        addComponent(Camera.class)
                .setOrtho(true)
                .setNear(0.3f)
                .setFar(100.0f)
                .setViewY(10.0f)
                .setFovY(90.0f);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (getEngine().getKeyRelease(GLFW_KEY_ESCAPE))
            getEngine().stop();
    }
}
