package io.scriptor.edge;

import io.scriptor.engine.Cycle;
import io.scriptor.engine.Engine;
import io.scriptor.engine.component.Transform;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.*;
import org.joml.Math;

public class Player extends Cycle {

    private enum Direction {
        NONE,
        /**
         * positive z
         */
        NORTH,
        /**
         * negative z
         */
        SOUTH,
        /**
         * negative x
         */
        WEST,
        /**
         * positive x
         */
        EAST,
    }

    private enum EdgeType {
        NONE,
        LEDGE_UP,
        HANG,
    }

    private Transform transform;

    private World world;
    private MainCamera camera;
    private Cube cube;

    private Direction direction = Direction.NONE;
    private float value, flipValue, endValue;
    private float dx, dy, dz;
    private boolean grounded;

    public Player(final @NotNull Engine engine, final @Nullable Cycle parent) {
        super(engine, parent);
    }

    @Override
    protected void onStart() {
        world = getEngine().getCycle("world", World.class);

        camera = getEngine().addCycle("main-camera", MainCamera.class, this);
        cube = getEngine().addCycle("cube", Cube.class, this);

        transform = addComponent(Transform.class).setTranslation(world.getSpawn());
    }

    @Override
    protected void onUpdate() {

        final var falling = isFalling();

        if (!grounded && !falling) {
            final var x = transform.getTranslation().x();
            final var y = transform.getTranslation().y();
            final var z = transform.getTranslation().z();
            transform.setTranslation(x, Math.floor(y), z);
        }

        grounded = !falling;

        if (falling) {
            final var d = getEngine().getDeltaTime() * 9.81f;
            transform.translate(0.0f, -d, 0.0f);

            if (transform.getTranslation().y() < -10.0f)
                transform.setTranslation(world.getSpawn());

            return;
        }

        final var vertical   = getEngine().getAxis("vertical");
        final var horizontal = getEngine().getAxis("horizontal");

        final var cubeTransform   = cube.getComponent(Transform.class);
        final var cameraTransform = camera.getComponent(Transform.class);

        final boolean invert;
        final float   force;

        switch (direction) {
            case NORTH -> {
                invert = vertical < 0.0f;
                force = invert ? -vertical : vertical;
            }
            case SOUTH -> {
                invert = vertical > 0.0f;
                force = invert ? vertical : -vertical;
            }
            case EAST -> {
                invert = horizontal < 0.0f;
                force = invert ? -horizontal : horizontal;
            }
            case WEST -> {
                invert = horizontal > 0.0f;
                force = invert ? horizontal : -horizontal;
            }
            default -> {
                final float px, py, pz;

                if (vertical > 0.0f) {
                    direction = Direction.NORTH;
                    px = 0.0f;
                    pz = 0.5f;
                    dx = 0.0f;
                    dz = 1.0f;
                } else if (vertical < 0.0f) {
                    direction = Direction.SOUTH;
                    px = 0.0f;
                    pz = -0.5f;
                    dx = 0.0f;
                    dz = -1.0f;
                } else if (horizontal > 0.0f) {
                    direction = Direction.EAST;
                    px = 0.5f;
                    pz = 0.0f;
                    dx = 1.0f;
                    dz = 0.0f;
                } else if (horizontal < 0.0f) {
                    direction = Direction.WEST;
                    px = -0.5f;
                    pz = 0.0f;
                    dx = -1.0f;
                    dz = 0.0f;
                } else {
                    cubeTransform.setPivot(0.0f, 0.0f, 0.0f);
                    cubeTransform.setRotation(new Quaternionf());
                    return;
                }

                final var edgeType = checkEdges(direction);
                if (edgeType == null) {
                    direction = Direction.NONE;
                    return;
                }

                switch (edgeType) {
                    case LEDGE_UP -> {
                        py = 0.5f;
                        dy = 1.0f;
                        flipValue = 1.5f;
                        endValue = 2.0f;
                    }
                    case HANG -> {
                        py = 0.5f;
                        dx = dz = 0.0f;
                        dy = 1.0f;
                        flipValue = 1.0f;
                        endValue = 1.0f;
                    }
                    case NONE -> {
                        py = -0.5f;
                        dy = 0.0f;
                        flipValue = 0.5f;
                        endValue = 1.0f;
                    }
                    default -> {
                        direction = Direction.NONE;
                        return;
                    }
                }

                cubeTransform.setPivot(px, py, pz);
                value = 0.0f;

                return;
            }
        }

        if (Math.abs(force) > 0.2f)
            move(force, invert);
        else
            relax();

        final var a = 0.5f * value * Math.PI_f;

        final Quaternionfc q = switch (direction) {
            case NORTH -> new Quaternionf().rotateAxis(a, new Vector3f(1, 0, 0));
            case SOUTH -> new Quaternionf().rotateAxis(a, new Vector3f(-1, 0, 0));
            case EAST -> new Quaternionf().rotateAxis(a, new Vector3f(0, 0, -1));
            case WEST -> new Quaternionf().rotateAxis(a, new Vector3f(0, 0, 1));
            default -> new Quaternionf();
        };

        cubeTransform.setRotation(q);

        final var cameraCenter = new Vector4f().mul(cubeTransform.getRawMatrix());
        cameraTransform.setTranslation(cameraCenter.xyz(new Vector3f()));
    }

    private void move(final float force, final boolean invert) {
        final var dv = getEngine().getDeltaTime() * force * 2.0f;
        if (invert) {
            value -= dv;
            if (value < 1e-5f) {
                direction = Direction.NONE;
            }
        } else {
            value += dv;
            if (value >= endValue) {
                transform.translate(dx, dy, dz);
                direction = Direction.NONE;
            }
        }
    }

    private void relax() {
        move(1.0f, value <= flipValue);
    }

    private boolean isFalling() {
        final var fy = transform.getTranslation().y();

        final var ix = Math.round(transform.getTranslation().x());
        final var iy = Math.round(transform.getTranslation().y());
        final var iz = Math.round(transform.getTranslation().z());

        final var dy = Math.abs(fy - iy);

        if (dy > 1e-1f)
            return true;

        return world.isAir(ix, iy - 1, iz);
    }

    private @Nullable Player.EdgeType checkEdges(final @NotNull Direction direction) {
        final var ix = (int) transform.getTranslation().x();
        final var iy = (int) transform.getTranslation().y();
        final var iz = (int) transform.getTranslation().z();

        final boolean miAir, hiAir, miAirI;

        switch (direction) {
            case NORTH -> {
                miAir = world.isAir(ix, iy, iz + 1);
                hiAir = world.isAir(ix, iy + 1, iz + 1);
                miAirI = world.isAir(ix, iy, iz - 1);
            }
            case SOUTH -> {
                miAir = world.isAir(ix, iy, iz - 1);
                hiAir = world.isAir(ix, iy + 1, iz - 1);
                miAirI = world.isAir(ix, iy, iz + 1);
            }
            case EAST -> {
                miAir = world.isAir(ix + 1, iy, iz);
                hiAir = world.isAir(ix + 1, iy + 1, iz);
                miAirI = world.isAir(ix - 1, iy, iz);
            }
            case WEST -> {
                miAir = world.isAir(ix - 1, iy, iz);
                hiAir = world.isAir(ix - 1, iy + 1, iz);
                miAirI = world.isAir(ix + 1, iy, iz);
            }
            default -> {
                return null;
            }
        }

        if (miAir && hiAir)
            return EdgeType.NONE;

        if (hiAir)
            return miAirI ? EdgeType.LEDGE_UP : null;

        return EdgeType.HANG;
    }
}
