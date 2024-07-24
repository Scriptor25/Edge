package io.scriptor;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

import io.scriptor.edge.World;
import io.scriptor.engine.Cycle;
import io.scriptor.engine.Engine;
import io.scriptor.engine.Model;

public class Edge extends Cycle {

    public static void main(String[] args) {
        final var engine = new Engine("Edge", 240, 320);
        engine.register(new Edge());
        engine.start();
        engine.destroy();
    }

    private World world;

    @Override
    public void onInit() {
        final var defaultModel = addComponent(Model.class,
                getEngine().createMesh("default_mesh"),
                getEngine().createMaterial("default_material", "default"));
        final var baseModel = addComponent(Model.class,
                getEngine().createMesh("base_mesh"),
                getEngine().createMaterial("base_material", "base"));
        final var endframeModel = addComponent(Model.class,
                getEngine().createMesh("endframe_mesh"),
                getEngine().createMaterial("endframe_material", "endframe"));

        world = World.load(ClassLoader.getSystemResourceAsStream("maps/first_contact.yaml"));
        world.generate(
                defaultModel.getMesh(),
                baseModel.getMesh(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                endframeModel.getMesh(),
                null);
    }

    @Override
    public void onUpdate() {
        if (getEngine().getKeyRelease(GLFW_KEY_ESCAPE))
            getEngine().stop();
    }
}
