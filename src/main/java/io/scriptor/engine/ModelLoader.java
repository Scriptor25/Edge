package io.scriptor.engine;

import org.joml.Vector3i;
import org.joml.Vector3ic;

import java.io.InputStream;

public class ModelLoader {

    private static Vector3ic asVector(final IYamlNode node) {
        final var xyz = node
                .stream()
                .map(e -> e.as(Integer.class).orElseThrow())
                .toArray(Integer[]::new);
        return new Vector3i(xyz[0], xyz[1], xyz[2]);
    }

    private ModelLoader() {
    }

    public static void loadModel(final InputStream stream) {
        final var node = IYamlNode.load(stream);

        final var id = node
                .get("id")
                .as(String.class)
                .orElseThrow();
        final var path = node
                .get("path")
                .as(String.class)
                .orElseThrow();

        final var boundsMin = asVector(node.get("bounds").get("min"));
        final var boundsMax = asVector(node.get("bounds").get("max"));

        final var triangles = node
                .get("triangles")
                .as(Boolean.class)
                .orElseThrow();
        final var textures = node
                .get("textures")
                .as(Boolean.class)
                .orElseThrow();
        final var normales = node
                .get("normales")
                .as(Boolean.class)
                .orElseThrow();

        final var meshIds = node
                .get("mesh-ids")
                .stream()
                .map(meshId -> meshId.as(String.class).orElseThrow())
                .toArray(String[]::new);
        final var materialIds = node
                .get("material-ids")
                .stream()
                .map(meshId -> meshId.as(String.class).orElseThrow())
                .toArray(String[]::new);
    }
}
