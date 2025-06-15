package io.scriptor.edge;

import org.jetbrains.annotations.NotNull;

public class Constant {

    /* MATERIAL NAME */

    public static final @NotNull String DEFAULT = "default";
    public static final @NotNull String BASE = "base";
    public static final @NotNull String RAINBOW = "rainbow";
    public static final @NotNull String RAINBOW_CUBE = "rainbow_cube";
    public static final @NotNull String RAINBOW_PRISM = "rainbow_prism";
    public static final @NotNull String PULSE_NORTH = "pulse_north";
    public static final @NotNull String PULSE_SOUTH = "pulse_south";
    public static final @NotNull String PULSE_EAST = "pulse_east";
    public static final @NotNull String PULSE_WEST = "pulse_west";
    public static final @NotNull String END_FRAME = "end_frame";

    /* MODEL NAME */

    public static final @NotNull String CUBE = "cube";

    /* UNIFORM NAME */

    public static final @NotNull String VIEW = "VIEW";
    public static final @NotNull String PROJECTION = "PROJECTION";
    public static final @NotNull String TRANSFORM = "TRANSFORM";
    public static final @NotNull String TIME = "TIME";
    public static final @NotNull String SUN_DIRECTION = "SUN_DIRECTION";
    public static final @NotNull String SPEED = "SPEED";

    private Constant() {
    }
}
