package io.scriptor.engine.component;

import io.scriptor.engine.Cycle;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Transform extends Component {

    private final Vector3f translation = new Vector3f();
    private final Quaternionf rotation = new Quaternionf();
    private final Vector3f scale = new Vector3f(1.0f);

    public Transform(final Cycle cycle) {
        super(cycle);
    }

    public Vector3f getTranslation() {
        return translation;
    }

    public Quaternionf getRotation() {
        return rotation;
    }

    public Vector3f getScale() {
        return scale;
    }

    public Matrix4fc getMatrix() {
        return new Matrix4f()
                .translate(translation)
                .rotate(rotation)
                .scale(scale);
    }
}
