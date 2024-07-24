package io.scriptor.engine;

import java.util.List;
import java.util.Vector;
import java.util.stream.Stream;

public class Material {

    private final List<Model> uses = new Vector<>();

    private final GLProgram program;

    public Material(final GLProgram program) {
        this.program = program;
    }

    public void use(final Model model) {
        uses.add(model);
    }

    public void bind() {
        program.bind();
    }

    public void unbind() {
        program.unbind();
    }

    public Stream<Model> stream() {
        return uses.stream();
    }

    public GLProgram getProgram() {
        return program;
    }
}
