package org.artifactory.util.stream;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * A "Bi-Directional" Optional to serve both ifPresent and ifNotPresent calls (i.e. if \ else kinda)
 * for that extra functional punch you never knew you needed.
 * <p>
 * NOTE: {@link java.util.Optional} is not inheritable, therefore this stupid hack.
 * Java 9 should improve this: https://bugs.openjdk.java.net/browse/JDK-8071670
 *
 * @author Dan Feldman
 */
public class BiOptional<T> {

    private Optional<T> optional;

    private BiOptional(Optional<T> optional) {
        this.optional = optional;
    }

    public static <T> BiOptional<T> of(Optional<T> optional) {
        return new BiOptional<>(optional);
    }

    public Optional<T> get() {
        return optional;
    }

    public BiOptional<T> ifPresent(Consumer<T> c) {
        optional.ifPresent(c);
        return this;
    }

    public BiOptional<T> ifPresent(Runnable r) {
        if (optional.isPresent()) {
            r.run();
        }
        return this;
    }

    public BiOptional<T> ifNotPresent(Runnable r) {
        if (!optional.isPresent())
            r.run();
        return this;
    }
}
