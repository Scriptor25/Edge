package io.scriptor.engine.component;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import io.scriptor.engine.Cycle;

public class Transform extends Component {

    public final Vector3f translation = new Vector3f();
    public final Quaternionf rotation = new Quaternionf();
    public final Vector3f scale = new Vector3f(1.0f);

    public Transform(final Cycle cycle) {
        super(cycle);
    }

    public Matrix4fc getMatrix() {
        return new Matrix4f()
                .translate(translation)
                .rotate(rotation)
                .scale(scale);
    }
}
