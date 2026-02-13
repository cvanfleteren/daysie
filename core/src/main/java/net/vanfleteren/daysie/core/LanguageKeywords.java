package net.vanfleteren.daysie.core;

import java.util.List;
import java.util.Set;

public record LanguageKeywords(
        Set<String> untilInclusive,
        Set<String> untilExclusive,
        Set<String> fromInclusive,
        Set<String> fromExclusive
) {
    public static final LanguageKeywords ENGLISH = new LanguageKeywords(
            Set.of("<=", "until"),
            Set.of("<", "before"),
            Set.of(">=", "since"),
            Set.of(">", "after")
    );

    public static final LanguageKeywords DUTCH = new LanguageKeywords(
            Set.of("<=", "tot en met"),
            Set.of("<", "voor", "tot"),
            Set.of(">=", "sinds", "vanaf"),
            Set.of(">", "na")
    );

    public static LanguageKeywords combine(List<LanguageKeywords> keywordsList) {
        return new LanguageKeywords(
                keywordsList.stream().flatMap(k -> k.untilInclusive().stream()).collect(java.util.stream.Collectors.toUnmodifiableSet()),
                keywordsList.stream().flatMap(k -> k.untilExclusive().stream()).collect(java.util.stream.Collectors.toUnmodifiableSet()),
                keywordsList.stream().flatMap(k -> k.fromInclusive().stream()).collect(java.util.stream.Collectors.toUnmodifiableSet()),
                keywordsList.stream().flatMap(k -> k.fromExclusive().stream()).collect(java.util.stream.Collectors.toUnmodifiableSet())
        );
    }
}
