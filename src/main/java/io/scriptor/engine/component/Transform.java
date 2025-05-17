package io.scriptor.engine.component;

import io.scriptor.engine.Cycle;
import org.jetbrains.annotations.NotNull;
import org.joml.*;
import org.joml.Math;

public class Transform extends Component {

    private final @NotNull Vector3f translation = new Vector3f();
    private final @NotNull Quaternionf rotation = new Quaternionf();
    private final @NotNull Vector3f scale = new Vector3f(1);

    private boolean dirty = false;
    private final @NotNull Matrix4f matrix = new Matrix4f();
    private final @NotNull Matrix4f inverse = new Matrix4f();

    public Transform(final @NotNull Cycle cycle) {
        super(cycle);
    }

    public @NotNull Vector3fc getTranslation() {
        return this.translation;
    }

    public @NotNull Quaternionfc getRotation() {
        return this.rotation;
    }

    public @NotNull Vector3fc getScale() {
        return this.scale;
    }

    public @NotNull Transform setTranslation(final @NotNull Vector3fc translation) {
        this.dirty = true;
        this.translation.set(translation);
        return this;
    }

    public @NotNull Transform setTranslation(final float x, final float y, final float z) {
        this.dirty = true;
        this.translation.set(x, y, z);
        return this;
    }

    public @NotNull Transform setRotation(final @NotNull Quaternionfc rotation) {
        this.dirty = true;
        this.rotation.set(rotation);
        return this;
    }

    public @NotNull Transform setScale(final @NotNull Vector3fc scale) {
        this.dirty = true;
        this.scale.set(scale);
        return this;
    }

    public @NotNull Transform setScale(final float scale) {
        this.dirty = true;
        this.scale.set(scale);
        return this;
    }

    public @NotNull Transform translate(final @NotNull Vector3fc delta) {
        this.dirty = true;
        this.translation.add(delta);
        return this;
    }

    public @NotNull Transform translate(final float dx, final float dy, final float dz) {
        this.dirty = true;
        this.translation.add(dx, dy, dz);
        return this;
    }

    public @NotNull Transform rotate(final float angle, final @NotNull Vector3fc axis) {
        this.dirty = true;
        this.rotation.rotateAxis(angle, axis);
        return this;
    }

    public @NotNull Transform rotateX(final float angle) {
        this.dirty = true;
        this.rotation.rotateX(Math.toRadians(angle));
        return this;
    }

    public @NotNull Transform rotateY(final float angle) {
        this.dirty = true;
        this.rotation.rotateY(Math.toRadians(angle));
        return this;
    }

    public @NotNull Transform scale(final float delta) {
        this.dirty = true;
        this.scale.mul(delta);
        return this;
    }

    public @NotNull Transform clampTranslation(final @NotNull Vector3fc min, final @NotNull Vector3fc max) {
        this.dirty = true;
        translation.x = Math.clamp(min.x(), max.x(), translation.x);
        translation.y = Math.clamp(min.y(), max.y(), translation.y);
        translation.z = Math.clamp(min.z(), max.z(), translation.z);
        return this;
    }

    public @NotNull Matrix4fc getMatrix() {
        if (dirty) {
            dirty = false;
            matrix.translationRotateScale(translation, rotation, scale);
            inverse.translationRotateScaleInvert(translation, rotation, scale);
        }
        return matrix;
    }

    public @NotNull Matrix4fc getInverse() {
        if (dirty) {
            dirty = false;
            matrix.translationRotateScale(translation, rotation, scale);
            inverse.translationRotateScaleInvert(translation, rotation, scale);
        }
        return inverse;
    }
}
