package io.scriptor.edge;

import io.scriptor.engine.Cycle;
import io.scriptor.engine.Engine;
import io.scriptor.engine.component.Model;
import io.scriptor.engine.component.Transform;
import io.scriptor.engine.data.IUniform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static io.scriptor.edge.Constant.*;

public class Cube extends Cycle {

    public Cube(final @NotNull Engine engine, final @Nullable Cycle parent) {
        super(engine, parent);
    }

    @Override
    protected void onStart() {
        addComponent(Transform.class);

        addComponent(Model.class, RAINBOW_CUBE, CUBE)
                .getMaterial()
                .ok(material -> material
                        .uniform(SPEED, IUniform.Uniform1f.class)
                        .set(10.0f));
    }
}
