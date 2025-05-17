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

    public @NotNull Camera setNear(final float near) {
        this.near = near;
        return this;
    }

    public @NotNull Camera setFar(final float far) {
        this.far = far;
        return this;
    }

    public @NotNull Camera setOrtho(final boolean ortho) {
        this.ortho = ortho;
        return this;
    }

    public @NotNull Camera setViewY(final float viewy) {
        this.viewy = viewy;
        return this;
    }

    public @NotNull Camera setFovY(final float fovy) {
        this.fovy = fovy;
        return this;
    }

    public @NotNull Matrix4fc getMatrix() {
        final var width  = getEngine().getWidth();
        final var height = getEngine().getHeight();
        final var aspect = (float) width / (float) height;
        final var matrix = new Matrix4f();
        if (ortho)
            matrix.setOrthoLH(-aspect * viewy, aspect * viewy, -viewy, viewy, near, far);
        else
            matrix.setPerspectiveLH(Math.toRadians(fovy), aspect, near, far);
        return matrix;
    }
}
