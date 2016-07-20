package net.novucs.ftop;

import java.util.Optional;

public final class StringUtils {

    private StringUtils() {
    }

    public static <T extends Enum<T>> Optional<T> parseEnum(Class<T> type, String name) {
        name = name.toUpperCase().replaceAll("\\s+", "_").replaceAll("\\W", "");
        try {
            return Optional.of(Enum.valueOf(type, name));
        } catch (IllegalArgumentException | NullPointerException e) {
            return Optional.empty();
        }
    }
}
