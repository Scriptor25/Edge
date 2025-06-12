package io.scriptor.edge;

import io.scriptor.engine.Cycle;
import io.scriptor.engine.Engine;
import io.scriptor.engine.component.Transform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class Player extends Cycle {

    private enum Direction {
        NONE,
        NORTH,
        SOUTH,
        WEST,
        EAST,
    }

    private enum Edge {
        NONE,
        LEDGE,
        FLOOR,
    }

    private Transform transform;

    private Cube cube;
    private World world;

    private Direction direction = Direction.NONE;
    private float value;

    public Player(final @NotNull Engine engine, final @Nullable Cycle parent) {
        super(engine, parent);
    }

    @Override
    protected void onStart() {
        world = getEngine().getCycle("world", World.class);

        getEngine().addCycle("main-camera", MainCamera.class, this);
        cube = getEngine().addCycle("cube", Cube.class, this);

        transform = addComponent(Transform.class).setTranslation(world.getSpawn());
    }

    @Override
    protected void onUpdate() {

        final var key_w = getEngine().getKey(GLFW_KEY_W);
        final var key_s = getEngine().getKey(GLFW_KEY_S);
        final var key_a = getEngine().getKey(GLFW_KEY_A);
        final var key_d = getEngine().getKey(GLFW_KEY_D);

        final var cubeTransform = cube.getComponent(Transform.class);

        final boolean force, invert;

        switch (direction) {
            case NORTH -> {
                force = key_w || key_s;
                invert = key_s;
            }
            case SOUTH -> {
                force = key_w || key_s;
                invert = key_w;
            }
            case WEST -> {
                force = key_a || key_d;
                invert = key_d;
            }
            case EAST -> {
                force = key_a || key_d;
                invert = key_a;
            }
            default -> {
                if (key_w) {
                    value = 0.0f;
                    direction = Direction.NORTH;
                    cubeTransform.setPivot(-0.5f, -0.5f, 0.0f);
                } else if (key_s) {
                    value = 0.0f;
                    direction = Direction.SOUTH;
                    cubeTransform.setPivot(0.5f, -0.5f, 0.0f);
                } else if (key_a) {
                    value = 0.0f;
                    direction = Direction.WEST;
                    cubeTransform.setPivot(0.0f, -0.5f, -0.5f);
                } else if (key_d) {
                    value = 0.0f;
                    direction = Direction.EAST;
                    cubeTransform.setPivot(0.0f, -0.5f, 0.5f);
                } else {
                    cubeTransform.setPivot(0.0f, 0.0f, 0.0f);
                    cubeTransform.setRotation(new Quaternionf());
                }

                return;
            }
        }

        if (force)
            force(invert);
        else
            relax();

        final var a = 0.5f * value * Math.PI_f;

        final Quaternionfc q = switch (direction) {
            case NORTH -> new Quaternionf().rotateAxis(a, new Vector3f(0, 0, 1));
            case SOUTH -> new Quaternionf().rotateAxis(a, new Vector3f(0, 0, -1));
            case WEST -> new Quaternionf().rotateAxis(a, new Vector3f(-1, 0, 0));
            case EAST -> new Quaternionf().rotateAxis(a, new Vector3f(1, 0, 0));
            default -> new Quaternionf();
        };

        cubeTransform.setRotation(q);
    }

    private void force(boolean invert) {
        final var d = getEngine().getDeltaTime() * 2;
        if (invert) {
            value -= d;
            if (value < 1e-5f) {
                direction = Direction.NONE;
            }
        } else {
            value += d;
            if (value >= 1.0f) {
                switch (direction) {
                    case NORTH -> transform.translate(-1.0f, 0.0f, 0.0f);
                    case SOUTH -> transform.translate(1.0f, 0.0f, 0.0f);
                    case WEST -> transform.translate(0.0f, 0.0f, -1.0f);
                    case EAST -> transform.translate(0.0f, 0.0f, 1.0f);
                }
                direction = Direction.NONE;
            }
        }
    }

    private void relax() {
        force(value <= 0.5f);
    }

    private @NotNull Edge checkEdges(final @NotNull Direction direction) {
        final var x = (int) transform.getTranslation().x();
        final var y = (int) transform.getTranslation().y();
        final var z = (int) transform.getTranslation().z();

        final boolean topAir, bottomAir;

        switch (direction) {
            case NORTH -> {
                topAir = world.isAir(x - 1, y, z);
                bottomAir = world.isAir(x - 1, y - 1, z);
            }
            case SOUTH -> {
                topAir = world.isAir(x + 1, y, z);
                bottomAir = world.isAir(x + 1, y - 1, z);
            }
            case WEST -> {
                topAir = world.isAir(x, y, z - 1);
                bottomAir = world.isAir(x, y - 1, z - 1);
            }
            case EAST -> {
                topAir = world.isAir(x, y, z + 1);
                bottomAir = world.isAir(x, y - 1, z + 1);
            }
            default -> {
                return Edge.NONE;
            }
        }

        if (!topAir)
            return Edge.LEDGE;

        if (!bottomAir)
            return Edge.FLOOR;

        return Edge.NONE;
    }
}
