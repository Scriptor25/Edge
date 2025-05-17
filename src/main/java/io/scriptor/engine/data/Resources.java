package io.scriptor.engine.data;

import io.scriptor.engine.Result;
import org.jetbrains.annotations.NotNull;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Optional;

public class Resources {

    @FunctionalInterface
    public interface OpenCallback<R> {
        @NotNull R apply(final @NotNull InputStream stream)
                throws Exception;
    }

    @FunctionalInterface
    public interface OpenVoidCallback {
        void apply(final @NotNull InputStream streamI)
                throws Exception;
    }

    private Resources() {
    }

    public static void openVoid(final @NotNull String name, final @NotNull OpenVoidCallback callback) {
        Optional.ofNullable(ClassLoader.getSystemResourceAsStream(name))
                .ifPresent(stream -> {
                    try (stream) {
                        callback.apply(stream);
                    } catch (final @NotNull Exception e) {
                        e.printStackTrace(System.err);
                    }
                });
    }

    public static <T> @NotNull Result<T> open(final @NotNull String name, final @NotNull OpenCallback<T> callback) {
        return Result
                .of(ClassLoader.getSystemResourceAsStream(name), new FileNotFoundException(name))
                .map(stream -> {
                    try (stream) {
                        return callback.apply(stream);
                    } catch (final @NotNull Exception e) {
                        return null;
                    }
                });
    }
}
