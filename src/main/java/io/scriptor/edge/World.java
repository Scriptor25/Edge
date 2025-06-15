package io.scriptor.edge;

import io.scriptor.engine.Cycle;
import io.scriptor.engine.Engine;
import io.scriptor.engine.ModelLoader;
import io.scriptor.engine.component.Model;
import io.scriptor.engine.component.Transform;
import io.scriptor.engine.data.IUniform.Uniform1f;
import io.scriptor.engine.data.Mesh;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3ic;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.scriptor.edge.Constant.*;
import static io.scriptor.engine.data.Resources.open;
import static io.scriptor.engine.data.Resources.openVoid;

public class World extends Cycle {

    public World(final @NotNull Engine engine, final @Nullable Cycle parent) {
        super(engine, parent);
    }

    private @Nullable Level level;
    private final @NotNull List<Prism> prisms = new ArrayList<>();

    private void addPrism(final @NotNull Vector3ic position) {
        final var prism = getEngine().addCycle("prism[" + prisms.size() + "]", Prism.class, this, position);
        prisms.add(prism);
    }

    public @NotNull Vector3fc getBounds() {
        return new Vector3f(Objects.requireNonNull(level).getBounds());
    }

    public @NotNull Vector3fc getSpawn() {
        return new Vector3f(Objects.requireNonNull(level).getSpawn());
    }

    @Override
    protected void onStart() {
        final var defaultMesh    = Mesh.create(DEFAULT).get();
        final var baseMesh       = Mesh.create(BASE).get();
        final var pulseNorthMesh = Mesh.create(PULSE_NORTH).get();
        final var pulseSouthMesh = Mesh.create(PULSE_SOUTH).get();
        final var pulseEastMesh  = Mesh.create(PULSE_EAST).get();
        final var pulseWestMesh  = Mesh.create(PULSE_WEST).get();
        final var endFrameMesh   = Mesh.create(END_FRAME).get();

        openVoid("model/cube.yaml", ModelLoader::loadModel);

        open("map/first_contact.yaml", Level::load)
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
                    lvl.getPrisms()
                       .forEach(this::addPrism);
                });

        addComponent(Transform.class);

        addComponent(Model.class, DEFAULT, DEFAULT);
        addComponent(Model.class, BASE, BASE);
        addComponent(Model.class, PULSE_NORTH, PULSE_NORTH);
        addComponent(Model.class, PULSE_SOUTH, PULSE_SOUTH);
        addComponent(Model.class, PULSE_EAST, PULSE_EAST);
        addComponent(Model.class, PULSE_WEST, PULSE_WEST);

        addComponent(Model.class, RAINBOW, END_FRAME)
                .getMaterial()
                .ok(material -> material
                        .uniform(SPEED, Uniform1f.class)
                        .set(10.0f));
    }

    public boolean isAir(int x, int y, int z) {
        return Objects.requireNonNull(level).isAir(x, y, z);
    }
}
