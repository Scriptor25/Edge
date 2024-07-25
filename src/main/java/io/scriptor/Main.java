package io.scriptor;

import io.scriptor.edge.Game;
import io.scriptor.engine.Engine;

public class Main {

    public static void main(String[] args) {
        final var engine = new Engine("Edge", 640, 320);
        engine.addCycle("edge", Game.class);
        engine.start();
        engine.destroy();
    }

    private Main() {
    }
}
