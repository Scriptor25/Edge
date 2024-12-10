package io.scriptor.engine.component;

import io.scriptor.engine.Cycle;
import org.joml.Math;
import org.joml.*;

public class Transform extends Component {

    private final Vector3f translation = new Vector3f();
    private final Quaternionf rotation = new Quaternionf();
    private final Vector3f scale = new Vector3f(1);

    private boolean dirty = false;
    private final Matrix4f matrix = new Matrix4f();
    private final Matrix4f inverse = new Matrix4f();

    public Transform(final Cycle cycle) {
        super(cycle);
    }

    public Vector3fc getTranslation() {
        return this.translation;
    }

    public Quaternionfc getRotation() {
        return this.rotation;
    }

    public Vector3f getScale() {
        return this.scale;
    }

    public Transform setTranslation(final Vector3f translation) {
        this.dirty = true;
        this.translation.set(translation);
        return this;
    }

    public Transform setRotation(final Quaternionf rotation) {
        this.dirty = true;
        this.rotation.set(rotation);
        return this;
    }

    public Transform setScale(final Vector3f scale) {
        this.dirty = true;
        this.scale.set(scale);
        return this;
    }

    public Transform setScale(final float scale) {
        this.dirty = true;
        this.scale.set(scale);
        return this;
    }

    public Transform translate(final Vector3f delta) {
        this.dirty = true;
        this.translation.add(delta);
        return this;
    }

    public Transform translate(final float dx, final float dy, final float dz) {
        this.dirty = true;
        this.translation.add(dx, dy, dz);
        return this;
    }

    public Transform rotate(final float angle, final Vector3fc axis) {
        this.dirty = true;
        this.rotation.rotateAxis(angle, axis);
        return this;
    }

    public Transform rotateX(final float angle) {
        this.dirty = true;
        this.rotation.rotateX(Math.toRadians(angle));
        return this;
    }

    public Transform rotateY(final float angle) {
        this.dirty = true;
        this.rotation.rotateY(Math.toRadians(angle));
        return this;
    }

    public Transform scale(final float delta) {
        this.dirty = true;
        this.scale.mul(delta);
        return this;
    }

    public Transform clampTranslation(final Vector3fc min, final Vector3fc max) {
        this.dirty = true;
        translation.x = Math.clamp(min.x(), max.x(), translation.x);
        translation.y = Math.clamp(min.y(), max.y(), translation.y);
        translation.z = Math.clamp(min.z(), max.z(), translation.z);
        return this;
    }

    public Matrix4fc getMatrix() {
        if (dirty) {
            dirty = false;
            matrix.translationRotateScale(translation, rotation, scale);
            inverse.translationRotateScaleInvert(translation, rotation, scale);
        }
        return matrix;
    }

    public Matrix4f getInverse() {
        if (dirty) {
            dirty = false;
            matrix.translationRotateScale(translation, rotation, scale);
            inverse.translationRotateScaleInvert(translation, rotation, scale);
        }
        return inverse;
    }
}
