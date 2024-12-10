package io.scriptor.edge;

import io.scriptor.engine.Cycle;
import io.scriptor.engine.Engine;
import io.scriptor.engine.ModelLoader;
import io.scriptor.engine.component.Model;
import io.scriptor.engine.data.Material;
import io.scriptor.engine.data.Mesh;
import io.scriptor.engine.data.Resources;
import io.scriptor.engine.gl.GLProgram;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3ic;

import java.util.ArrayList;
import java.util.List;

public class World extends Cycle {

    private static final String DEFAULT = "default";
    private static final String BASE = "base";
    private static final String RAINBOW = "rainbow";
    private static final String PULSE_NORTH = "pulse_north";
    private static final String PULSE_SOUTH = "pulse_south";
    private static final String PULSE_EAST = "pulse_east";
    private static final String PULSE_WEST = "pulse_west";
    private static final String END_FRAME = "end_frame";

    public World(final Engine engine) {
        super(engine);
    }

    private Level level;
    private final List<Prism> prisms = new ArrayList<>();

    private void addPrism(final Vector3ic position) {
        final var prism = getEngine().addCycle("prism[" + prisms.size() + "]", Prism.class, position);
        prisms.add(prism);
    }

    public Vector3fc getBounds() {
        return new Vector3f(level.getBounds());
    }

    @Override
    public void onStart() {
        super.onStart();

        Resources.open("shader/default.yaml", stream -> Material.create(DEFAULT, GLProgram.create(stream)));
        Resources.open("shader/base.yaml", stream -> Material.create(BASE, GLProgram.create(stream)));
        Resources.open("shader/rainbow.yaml", stream -> Material.create(RAINBOW, GLProgram.create(stream)));
        Resources.open("shader/pulse_north.yaml", stream -> Material.create(PULSE_NORTH, GLProgram.create(stream)));
        Resources.open("shader/pulse_south.yaml", stream -> Material.create(PULSE_SOUTH, GLProgram.create(stream)));
        Resources.open("shader/pulse_east.yaml", stream -> Material.create(PULSE_EAST, GLProgram.create(stream)));
        Resources.open("shader/pulse_west.yaml", stream -> Material.create(PULSE_WEST, GLProgram.create(stream)));

        final var defaultMesh = Mesh.create(DEFAULT).get();
        final var baseMesh = Mesh.create(BASE).get();
        final var pulseNorthMesh = Mesh.create(PULSE_NORTH).get();
        final var pulseSouthMesh = Mesh.create(PULSE_SOUTH).get();
        final var pulseEastMesh = Mesh.create(PULSE_EAST).get();
        final var pulseWestMesh = Mesh.create(PULSE_WEST).get();
        final var endFrameMesh = Mesh.create(END_FRAME).get();

        Resources.openVoid("model/cube.yaml", ModelLoader::loadModel);

        Resources
                .open("map/first_contact.yaml", Level::load)
                .ok(lvl -> {
                    level = lvl;
                    lvl.generate(
                            defaultMesh,
                            baseMesh,
                            pulseNorthMesh,
                            pulseSouthMesh,
                            pulseEastMesh,
                            pulseWestMesh,
                            null,
                            null,
                            null,
                            endFrameMesh,
                            null);
                    prisms.clear();
                    lvl
                            .getPrisms()
                            .forEach(this::addPrism);
                });

        addComponent(Model.class, DEFAULT, DEFAULT);
        addComponent(Model.class, BASE, BASE);
        addComponent(Model.class, PULSE_NORTH, PULSE_NORTH);
        addComponent(Model.class, PULSE_SOUTH, PULSE_SOUTH);
        addComponent(Model.class, PULSE_EAST, PULSE_EAST);
        addComponent(Model.class, PULSE_WEST, PULSE_WEST);
        addComponent(Model.class, RAINBOW, END_FRAME);
    }
}
