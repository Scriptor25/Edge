package io.scriptor.engine.data;

import io.scriptor.engine.Result;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Optional;

public class Resources {

    @FunctionalInterface
    public interface OpenCallback<R> {
        R apply(final InputStream stream) throws Exception;
    }

    @FunctionalInterface
    public interface OpenVoidCallback {
        void apply(final InputStream streamI) throws Exception;
    }

    private Resources() {
    }

    public static void openVoid(final String name, final OpenVoidCallback callback) {
        Optional
                .ofNullable(ClassLoader.getSystemResourceAsStream(name))
                .ifPresent(stream -> {
                    try (stream) {
                        callback.apply(stream);
                    } catch (final Exception e) {
                        e.printStackTrace(System.err);
                    }
                });
    }

    public static <T> Result<T> open(final String name, final OpenCallback<T> callback) {
        return Result
                .of(ClassLoader.getSystemResourceAsStream(name), new FileNotFoundException(name))
                .map(stream -> {
                    try (stream) {
                        return callback.apply(stream);
                    } catch (final Exception e) {
                        return null;
                    }
                });
    }
}
