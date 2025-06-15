package io.scriptor.engine.data;

import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import static org.lwjgl.opengl.GL41.*;

public interface IUniform {

    void apply(final int program, final int location);


    class Uniform1f implements IUniform {

        public float x;

        public Uniform1f() {
            this.x = 0.0f;
        }

        public Uniform1f(final float x) {
            this.x = x;
        }

        @Override
        public void apply(final int program, final int location) {
            glProgramUniform1f(program, location, x);
        }

        public void set(final float x) {
            this.x = x;
        }
    }

    class Uniform2f implements IUniform {

        public float x, y;

        public Uniform2f() {
            this.x = 0.0f;
            this.y = 0.0f;
        }

        public Uniform2f(final float x, final float y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void apply(final int program, final int location) {
            glProgramUniform2f(program, location, x, y);
        }

        public void set(final float x, final float y) {
            this.x = x;
            this.y = y;
        }
    }

    class Uniform3f implements IUniform {

        public float x, y, z;

        public Uniform3f() {
            this.x = 0.0f;
            this.y = 0.0f;
            this.z = 0.0f;
        }

        public Uniform3f(final float x, final float y, final float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public void apply(final int program, final int location) {
            glProgramUniform3f(program, location, x, y, z);
        }

        public void set(final float x, final float y, final float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    class Uniform4f implements IUniform {

        public float x, y, z, w;

        public Uniform4f() {
            this.x = 0.0f;
            this.y = 0.0f;
            this.z = 0.0f;
            this.w = 0.0f;
        }

        public Uniform4f(final float x, final float y, final float z, final float w) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.w = w;
        }

        @Override
        public void apply(final int program, final int location) {
            glProgramUniform4f(program, location, x, y, z, w);
        }

        public void set(final float x, final float y, final float z, final float w) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.w = w;
        }
    }

    class Uniform1i implements IUniform {

        public int x;

        public Uniform1i() {
            this.x = 0;
        }

        public Uniform1i(final int x) {
            this.x = x;
        }

        @Override
        public void apply(final int program, final int location) {
            glProgramUniform1i(program, location, x);
        }

        public void set(final int x) {
            this.x = x;
        }
    }

    class Uniform2i implements IUniform {

        public int x, y;

        public Uniform2i() {
            this.x = 0;
            this.y = 0;
        }

        public Uniform2i(final int x, final int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void apply(final int program, final int location) {
            glProgramUniform2i(program, location, x, y);
        }

        public void set(final int x, final int y) {
            this.x = x;
            this.y = y;
        }
    }

    class Uniform3i implements IUniform {

        public int x, y, z;

        public Uniform3i() {
            this.x = 0;
            this.y = 0;
            this.z = 0;
        }

        public Uniform3i(final int x, final int y, final int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        @Override
        public void apply(final int program, final int location) {
            glProgramUniform3i(program, location, x, y, z);
        }

        public void set(final int x, final int y, final int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    class Uniform4i implements IUniform {

        public int x, y, z, w;

        public Uniform4i() {
            this.x = 0;
            this.y = 0;
            this.z = 0;
            this.w = 0;
        }

        public Uniform4i(final int x, final int y, final int z, final int w) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.w = w;
        }

        @Override
        public void apply(final int program, final int location) {
            glProgramUniform4i(program, location, x, y, z, w);
        }

        public void set(final int x, final int y, final int z, final int w) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.w = w;
        }
    }

    class UniformMatrix4f implements IUniform {

        public boolean transpose;
        public final Matrix4f value = new Matrix4f();

        public UniformMatrix4f() {
            this.transpose = false;
        }

        public UniformMatrix4f(final boolean transpose, final @NotNull Matrix4fc value) {
            this.transpose = transpose;
            this.value.set(value);
        }

        @Override
        public void apply(final int program, final int location) {
            glProgramUniformMatrix4fv(program, location, transpose, value.get(new float[16]));
        }

        public void set(final boolean transpose, final @NotNull Matrix4fc value) {
            this.transpose = transpose;
            this.value.set(value);
        }
    }
}
