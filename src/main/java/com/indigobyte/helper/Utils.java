package com.indigobyte.helper;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Utils {
    public static <T> String createMessage(
            @NotNull Collection<T> arg1,
            @NotNull String arg1Description,
            @NotNull Collection<T> arg2,
            @NotNull String arg2Description
    ) {
        StringBuilder sb = new StringBuilder();
        {
            Set<T> uniqueFor1 = new LinkedHashSet<>(arg1);
            uniqueFor1.removeAll(arg2);
            if (!uniqueFor1.isEmpty()) {
                getUniqueItemsFromArgument(sb, uniqueFor1, arg1Description, arg2Description);
            }
        }
        {
            Set<T> uniqueFor2 = new LinkedHashSet<>(arg2);
            uniqueFor2.removeAll(arg1);
            if (!uniqueFor2.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                getUniqueItemsFromArgument(sb, uniqueFor2, arg2Description, arg1Description);
            }
        }
        return sb.toString();
    }

    private static <T> void getUniqueItemsFromArgument(StringBuilder sb, Set<T> uniqueFor1, @NotNull String arg1Description, @NotNull String arg2Description) {
        sb.append(arg1Description);
        sb.append(" contains ");
        sb.append(uniqueFor1.size());
        sb.append(" unique items not found in ");
        sb.append(arg2Description);
        sb.append(":\n");
        sb.append(Utils.collectionToString(uniqueFor1, "\n"));
    }

    @NotNull
    public static <E> String collectionToString(@Nullable Collection<E> items, @NotNull String delimiter) {
        if (items == null) {
            return "null";
        }
        if (items.isEmpty()) {
            return "[empty collection]";
        }
        return join(items, delimiter);
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
