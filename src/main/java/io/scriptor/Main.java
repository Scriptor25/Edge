package io.scriptor;

import io.scriptor.edge.Game;
import io.scriptor.engine.Engine;
import org.jetbrains.annotations.NotNull;

public class Main {

    public static void main(final @NotNull String @NotNull [] args) {
        final var engine = new Engine("Edge", 360, 480);
        engine.addCycle("edge", Game.class, null);
        engine.start();
        engine.destroy();
    }

    private Main() {
    }
}
