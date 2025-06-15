package io.scriptor.engine.component;

import io.scriptor.engine.Cycle;
import org.jetbrains.annotations.NotNull;
import org.joml.Math;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

public class Camera extends Component {

    private float near = 0.3f;
    private float far = 100.0f;
    private boolean ortho = false;
    private float viewy = 1.0f;
    private float fovy = 90.0f;
    private float aspect = 1.0f;

    private boolean dirty = true;
    private final Matrix4f matrix = new Matrix4f();

    public Camera(final @NotNull Cycle cycle) {
        super(cycle);
    }

    public float getNear() {
        return near;
    }

    public float getFar() {
        return far;
    }

    public boolean isOrtho() {
        return ortho;
    }

    public float getViewY() {
        return viewy;
    }

    public float getFovY() {
        return fovy;
    }

    public float getAspect() {
        return aspect;
    }

    public @NotNull Camera setNear(final float near) {
        this.dirty = true;
        this.near = near;
        return this;
    }

    public @NotNull Camera setFar(final float far) {
        this.dirty = true;
        this.far = far;
        return this;
    }

    public @NotNull Camera setOrtho(final boolean ortho) {
        this.dirty = true;
        this.ortho = ortho;
        return this;
    }

    public @NotNull Camera setViewY(final float viewy) {
        this.dirty = true;
        this.viewy = viewy;
        return this;
    }

    public @NotNull Camera setFovY(final float fovy) {
        this.dirty = true;
        this.fovy = fovy;
        return this;
    }

    public @NotNull Camera setAspect(final float aspect) {
        this.dirty = true;
        this.aspect = aspect;
        return this;
    }

    public @NotNull Matrix4fc getMatrix() {
        if (!dirty)
            return matrix;

        dirty = false;

        if (ortho)
            matrix.setOrthoLH(-aspect * viewy, aspect * viewy, -viewy, viewy, near, far);
        else
            matrix.setPerspectiveLH(Math.toRadians(fovy), aspect, near, far);

        return matrix;
    }
}
