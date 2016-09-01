package net.novucs.ftop.util;

import org.bukkit.Material;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public final class GenericUtils {

    private GenericUtils() {
    }

    public static <E> List<E> castList(Class<? extends E> type, List<?> toCast) throws ClassCastException {
        return toCast.stream().map(type::cast).collect(Collectors.toList());
    }

    public static Optional<List> getList(Map<?, ?> input, Object key) {
        return getValue(List.class, input, key);
    }

    public static Optional<Material> getMaterial(Map<?, ?> input, Object key) {
        return getEnum(Material.class, input, key);
    }

    public static <T extends Enum<T>> Optional<T> getEnum(Class<T> type, Map<?, ?> input, Object key) {
        Optional<String> name = getString(input, key);
        if (name.isPresent()) {
            return parseEnum(type, name.get());
        }
        return Optional.empty();
    }

    public static Optional<Boolean> getBoolean(Map<?, ?> input, Object key) {
        return getValue(Boolean.class, input, key);
    }

    public static Optional<Integer> getInt(Map<?, ?> input, Object key) {
        return getValue(Integer.class, input, key);
    }

    public static Optional<String> getString(Map<?, ?> input, Object key) {
        return getValue(String.class, input, key);
    }

    public static Optional<Map> getMap(Map<?, ?> input, Object key) {
        return getValue(Map.class, input, key);
    }

    public static <T> Optional<T> getValue(Class<T> clazz, Map<?, ?> input, Object key) {
        Object target = input.get(key);
        if (target == null || !clazz.isInstance(target)) {
            return Optional.empty();
        }
        return Optional.of((T) target);
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
