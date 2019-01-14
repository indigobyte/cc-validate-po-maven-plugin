package com.indigobyte.helper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Utils {
    public static <T> String createMessage(@NotNull Collection<T> given, @NotNull Collection<T> found) {
        List<T> missingIds = new ArrayList<>(given);
        missingIds.removeAll(found);
        return "Given: " + Utils.collectionToString(given) + ", found: " +
                Utils.collectionToString(found) + ". Missing: " + Utils.collectionToString(missingIds);
    }

    @NotNull
    public static <E> String collectionToString(@Nullable Collection<E> items) {
        if (items == null) {
            return "null";
        }
        if (items.isEmpty()) {
            return "[empty collection]";
        }
        return join(items, ", ");
    }

    @NotNull
    public static <T> String join(@NotNull Collection<T> items, @NotNull String delimiter) {
        return join(items, delimiter, "", "");
    }

    @NotNull
    public static <T> String join(@NotNull Collection<T> items, @NotNull String delimiter, @NotNull String prefix, @NotNull String suffix) {
        return items.stream()
                .map(Objects::toString)
                .collect(Collectors.joining(delimiter, prefix, suffix));
    }
}
