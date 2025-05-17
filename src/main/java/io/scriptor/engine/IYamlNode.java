package io.scriptor.engine;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.io.Reader;
import java.util.*;
import java.util.stream.Stream;

public interface IYamlNode extends Iterable<IYamlNode> {

    static @NotNull IYamlNode load(final @NotNull String yaml) {
        final Map<String, ?> map = new Yaml().load(yaml);
        return load(null, map);
    }

    static @NotNull IYamlNode load(final @NotNull InputStream yaml) {
        final Map<String, ?> map = new Yaml().load(yaml);
        return load(null, map);
    }

    static @NotNull IYamlNode load(final @NotNull Reader yaml) {
        final Map<String, ?> map = new Yaml().load(yaml);
        return load(null, map);
    }

    static @NotNull MapNode load(final @Nullable String name, final @NotNull Map<String, ?> yaml) {
        final Map<String, IYamlNode> data = new HashMap<>();
        yaml.forEach((key, val) -> data.put(key, load(key, val)));
        return new MapNode(name, data);
    }

    static @NotNull ListNode load(final @Nullable String name, final @NotNull List<?> yaml) {
        return new ListNode(name, yaml.stream().map(val -> load(null, val)).toList());
    }

    @SuppressWarnings("unchecked")
    static @NotNull IYamlNode load(final @Nullable String name, final @Nullable Object yaml) {
        if (yaml instanceof Map<?, ?> map)
            return load(name, (Map<String, ?>) map);
        if (yaml instanceof List<?> list)
            return load(name, list);
        if (yaml != null)
            return new DataNode(name, yaml);
        return new EmptyNode(name);
    }

    record MapNode(@Nullable String name, @NotNull Map<String, @NotNull IYamlNode> data) implements IYamlNode {

        @Override
        public @NotNull Iterator<IYamlNode> iterator() {
            return data.values().iterator();
        }

        @Override
        public @NotNull IYamlNode get(final @NotNull String key) {
            if (data.containsKey(key))
                return data.get(key);
            return new EmptyNode(key);
        }

        @Override
        public @NotNull Stream<IYamlNode> stream() {
            return data.values().stream();
        }
    }

    record ListNode(@Nullable String name, @NotNull List<IYamlNode> data) implements IYamlNode {

        @Override
        public @NotNull Iterator<IYamlNode> iterator() {
            return data.iterator();
        }

        @Override
        public @NotNull IYamlNode get(final int index) {
            if (index < 0 || index >= data.size())
                return new EmptyNode(null);
            return data.get(index);
        }

        @Override
        public @NotNull Stream<IYamlNode> stream() {
            return data.stream();
        }
    }

    record DataNode(@Nullable String name, @NotNull Object data) implements IYamlNode {

        @Override
        public @NotNull Iterator<IYamlNode> iterator() {
            return Collections.emptyIterator();
        }

        @Override
        public <E> @NotNull Result<E> as(final @NotNull Class<E> type) {
            try {
                return Result.of(type.cast(data));
            } catch (final @NotNull ClassCastException e) {
                return Result.err(e);
            }
        }
    }

    record EmptyNode(@Nullable String name) implements IYamlNode {

        @Override
        public @NotNull Iterator<IYamlNode> iterator() {
            return Collections.emptyIterator();
        }
    }

    @Nullable String name();

    default @NotNull IYamlNode get(final @NotNull String key) {
        return new EmptyNode(key);
    }

    default @NotNull IYamlNode get(final int index) {
        return new EmptyNode(null);
    }

    default <E> @NotNull Result<E> as(final @NotNull Class<E> type) {
        return Result.err(new ClassCastException("cannot cast value of non-data node to %s".formatted(type)));
    }

    default @NotNull Stream<IYamlNode> stream() {
        return Stream.empty();
    }
}
