package io.scriptor.engine;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Result<T> {

    @FunctionalInterface
    public interface Converter<T, R> {
        @Nullable R convert(final @NotNull T t);
    }

    @FunctionalInterface
    public interface BooleanConverter<R> {
        @Nullable R convert(final boolean t);
    }

    public static <T> @NotNull Result<T> of(final @NotNull T value) {
        return new Result<>(value, false, null);
    }

    public static <T> @NotNull Result<T> of(final @Nullable T value, final @NotNull Throwable throwable) {
        return new Result<>(value, value == null, throwable);
    }

    public static <T> @NotNull Result<T> err(final @NotNull Throwable throwable) {
        return new Result<>(null, true, throwable);
    }

    private final @Nullable T value;

    private final boolean error;
    private final @Nullable Throwable throwable;

    private Result(final @Nullable T value, final boolean error, final @Nullable Throwable throwable) {
        this.value = value;
        this.error = error;
        this.throwable = throwable;
    }

    public @NotNull T get() {
        if (error)
            throw new IllegalStateException(throwable);
        return Objects.requireNonNull(value);
    }

    public boolean ok() {
        return !error;
    }

    public void ok(final @NotNull Consumer<T> consumer) {
        if (!error)
            consumer.accept(value);
    }

    public @NotNull T or(final @NotNull Supplier<T> supplier) {
        if (error)
            return supplier.get();
        return Objects.requireNonNull(value);
    }

    public @NotNull T or(final @NotNull T result) {
        if (error)
            return result;
        return Objects.requireNonNull(value);
    }

    public <R> @NotNull Result<R> map(final @NotNull Converter<T, R> converter) {
        if (error)
            return Result.err(Objects.requireNonNull(throwable));
        return Result.of(converter.convert(Objects.requireNonNull(value)), new NullPointerException());
    }

    public <R> @NotNull Result<R> mapBoolean(final @NotNull BooleanConverter<R> converter) {
        if (error)
            return Result.err(Objects.requireNonNull(throwable));
        try {
            return Result.of(converter.convert((Boolean) Objects.requireNonNull(value)), new NullPointerException());
        } catch (final @NotNull ClassCastException e) {
            return Result.err(e);
        }
    }
}
