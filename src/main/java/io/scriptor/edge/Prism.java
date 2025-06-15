package io.scriptor.edge;

import io.scriptor.engine.Cycle;
import io.scriptor.engine.Engine;
import io.scriptor.engine.component.Model;
import io.scriptor.engine.component.Transform;
import io.scriptor.engine.data.IUniform.Uniform1f;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;

import static io.scriptor.edge.Constant.*;

public class Prism extends Cycle {

    private final @NotNull Vector3fc position;

    private Transform transform;

    public Prism(final @NotNull Engine engine, final @Nullable Cycle parent, final @NotNull Vector3i position) {
        super(engine, parent);
        this.position = new Vector3f(position);
    }

    @Override
    protected void onStart() {
        transform = addComponent(Transform.class).setScale(0.4f);

        addComponent(Model.class, RAINBOW_PRISM, CUBE)
                .getMaterial()
                .ok(material -> material
                        .uniform(SPEED, Uniform1f.class)
                        .set(20.0f));
    }

    @Override
    protected void onUpdate() {
        final var time = getEngine().getTime();
        final var dy   = Math.sin(time * 2.0f) * 0.2f;

        transform
                .setTranslation(position.add(0, dy, 0, new Vector3f()))
                .setRotation(time * 40.0f, new Vector3f(0, 1, 0));
    }
}
