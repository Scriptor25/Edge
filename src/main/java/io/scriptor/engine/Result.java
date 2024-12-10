package io.scriptor.engine;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Result<T> {

    @FunctionalInterface
    public interface Converter<T, R> {
        R convert(final T t);
    }

    @FunctionalInterface
    public interface BooleanConverter<R> {
        R convert(final boolean t);
    }

    public static <T> Result<T> of(final T value) {
        return new Result<>(value, false, null);
    }

    public static <T> Result<T> of(final T value, final Throwable throwable) {
        return new Result<>(value, value == null, throwable);
    }

    public static <T> Result<T> err(final Throwable throwable) {
        return new Result<>(null, true, throwable);
    }

    private final T value;

    private final boolean error;
    private final Throwable throwable;

    private Result(final T value, final boolean error, final Throwable throwable) {
        this.value = value;
        this.error = error;
        this.throwable = throwable;
    }

    public T get() {
        if (error)
            throw new IllegalStateException(throwable);
        return value;
    }

    public boolean ok() {
        return !error;
    }

    public void ok(final Consumer<T> consumer) {
        if (!error)
            consumer.accept(value);
    }

    public T or(final Supplier<T> supplier) {
        if (error)
            return supplier.get();
        return value;
    }

    public T or(final T result) {
        if (error)
            return result;
        return value;
    }

    public <R> Result<R> map(final Converter<T, R> converter) {
        if (error)
            return Result.err(throwable);
        return Result.of(converter.convert(value));
    }

    public <R> Result<R> mapBoolean(final BooleanConverter<R> converter) {
        if (error)
            return Result.err(throwable);
        try {
            return Result.of(converter.convert((Boolean) value));
        } catch (ClassCastException e) {
            return Result.err(e);
        }
    }
}
