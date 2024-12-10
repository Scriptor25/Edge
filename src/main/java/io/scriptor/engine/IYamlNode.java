package io.scriptor.engine;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.io.Reader;
import java.util.*;
import java.util.stream.Stream;

public interface IYamlNode extends Iterable<IYamlNode> {

    static IYamlNode load(final String yaml) {
        final Map<?, ?> map = new Yaml().load(yaml);
        return load(null, map);
    }

    static IYamlNode load(final InputStream yaml) {
        final Map<?, ?> map = new Yaml().load(yaml);
        return load(null, map);
    }

    static IYamlNode load(final Reader yaml) {
        final Map<?, ?> map = new Yaml().load(yaml);
        return load(null, map);
    }

    static MapNode load(final String name, final Map<?, ?> yaml) {
        final Map<String, IYamlNode> data = new HashMap<>();
        yaml.forEach((key, val) -> data.put((String) key, load((String) key, val)));
        return new MapNode(name, data);
    }

    static ListNode load(final String name, final List<?> yaml) {
        return new ListNode(name, yaml.stream().map(val -> load(null, val)).toList());
    }

    static IYamlNode load(final String name, final Object yaml) {
        if (yaml instanceof Map<?, ?> map)
            return load(name, map);
        if (yaml instanceof List<?> list)
            return load(name, list);
        return new DataNode(name, yaml);
    }

    record MapNode(String name, Map<String, IYamlNode> data) implements IYamlNode {

        @Override
        public Iterator<IYamlNode> iterator() {
            return data.values().iterator();
        }

        @Override
        public IYamlNode get(final String key) {
            if (data.containsKey(key))
                return data.get(key);
            return new EmptyNode(key);
        }

        @Override
        public Stream<IYamlNode> stream() {
            return data.values().stream();
        }
    }

    record ListNode(String name, List<IYamlNode> data) implements IYamlNode {

        @Override
        public Iterator<IYamlNode> iterator() {
            return data.iterator();
        }

        @Override
        public IYamlNode get(final int index) {
            if (index < 0 || index >= data.size())
                return new EmptyNode(null);
            return data.get(index);
        }

        @Override
        public Stream<IYamlNode> stream() {
            return data.stream();
        }
    }

    record DataNode(String name, Object data) implements IYamlNode {

        @Override
        public Iterator<IYamlNode> iterator() {
            return Collections.emptyIterator();
        }

        @Override
        public <E> Result<E> as(final Class<E> type) {
            try {
                return Result.of(type.cast(data));
            } catch (ClassCastException e) {
                return Result.err(e);
            }
        }
    }

    record EmptyNode(String name) implements IYamlNode {

        @Override
        public Iterator<IYamlNode> iterator() {
            return Collections.emptyIterator();
        }
    }

    String name();

    default IYamlNode get(final String key) {
        return new EmptyNode(key);
    }

    default IYamlNode get(final int index) {
        return new EmptyNode(null);
    }

    default <E> Result<E> as(final Class<E> type) {
        return Result.err(new ClassCastException("cannot cast value of non-data node to %s".formatted(type)));
    }

    default Stream<IYamlNode> stream() {
        return Stream.empty();
    }
}
