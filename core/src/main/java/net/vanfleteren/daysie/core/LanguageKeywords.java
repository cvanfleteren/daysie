package net.vanfleteren.daysie.core;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record LanguageKeywords(
        Set<String> untilInclusive,
        Set<String> untilExclusive,
        Set<String> fromInclusive,
        Set<String> fromExclusive,
        Set<String> rangeConnectorsInclusive,
        Set<String> rangeConnectorsExclusive
) {
    public static final LanguageKeywords ENGLISH = new LanguageKeywords(
            Set.of("<=", "until"),
            Set.of("<", "before"),
            Set.of(">=", "since"),
            Set.of(">", "after"),
            Set.of("to","-"),
            Set.of()
    );

    public static final LanguageKeywords DUTCH = new LanguageKeywords(
            Set.of("<=", "tot en met"),
            Set.of("<", "voor", "tot"),
            Set.of(">=", "sinds", "vanaf"),
            Set.of(">", "na"),
            Set.of("tot", "t/m", "tot en met","-"),
            Set.of()
    );

    public static LanguageKeywords combine(List<LanguageKeywords> keywordsList) {
        return new LanguageKeywords(
                keywordsList.stream().flatMap(k -> k.untilInclusive().stream()).collect(Collectors.toUnmodifiableSet()),
                keywordsList.stream().flatMap(k -> k.untilExclusive().stream()).collect(Collectors.toUnmodifiableSet()),
                keywordsList.stream().flatMap(k -> k.fromInclusive().stream()).collect(Collectors.toUnmodifiableSet()),
                keywordsList.stream().flatMap(k -> k.fromExclusive().stream()).collect(Collectors.toUnmodifiableSet()),
                keywordsList.stream().flatMap(k -> k.rangeConnectorsInclusive().stream()).collect(Collectors.toUnmodifiableSet()),
                keywordsList.stream().flatMap(k -> k.rangeConnectorsExclusive().stream()).collect(Collectors.toUnmodifiableSet())
        );
    }
}
