package net.novucs.ftop.util;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Consumer;

public interface TreeIterator<E> extends Iterator<E> {

    boolean hasPrevious();

    E previous();

    default void forEachPrevious(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        while (hasPrevious()) {
            action.accept(previous());
        }
    }
}
