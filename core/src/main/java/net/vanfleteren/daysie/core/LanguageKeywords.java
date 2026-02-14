package net.vanfleteren.daysie.core;

import java.time.DayOfWeek;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record LanguageKeywords(
        Set<String> untilInclusive,
        Set<String> untilExclusive,
        Set<String> fromInclusive,
        Set<String> fromExclusive,
        Set<String> rangeConnectorsInclusive,
        Set<String> rangeConnectorsExclusive,
        Set<String> today,
        Set<String> yesterday,
        Set<String> tomorrow,
        Set<String> last,
        Map<String, ChronoUnit> chronoUnits,
        Map<String, DayOfWeek> daysOfWeek
    ) {
        public static final LanguageKeywords ENGLISH = new LanguageKeywords(
                Set.of("<=", "until"),
                Set.of("<", "before"),
                Set.of(">=", "since"),
                Set.of(">", "after"),
                Set.of("to","-"),
                Set.of(),
                Set.of("today"),
                Set.of("yesterday"),
                Set.of("tomorrow"),
                Set.of("last", "previous"),
                Map.of(
                        "year", ChronoUnit.YEARS,
                        "years", ChronoUnit.YEARS,
                        "quarter", ChronoUnit.MONTHS,
                        "quarters", ChronoUnit.MONTHS,
                        "month", ChronoUnit.MONTHS,
                        "months", ChronoUnit.MONTHS,
                        "week", ChronoUnit.WEEKS,
                        "weeks", ChronoUnit.WEEKS,
                        "day", ChronoUnit.DAYS,
                        "days", ChronoUnit.DAYS
                ),
                Map.of(
                        "monday", DayOfWeek.MONDAY,
                        "tuesday", DayOfWeek.TUESDAY,
                        "wednesday", DayOfWeek.WEDNESDAY,
                        "thursday", DayOfWeek.THURSDAY,
                        "friday", DayOfWeek.FRIDAY,
                        "saturday", DayOfWeek.SATURDAY,
                        "sunday", DayOfWeek.SUNDAY
                )
        );

        public static final LanguageKeywords DUTCH = new LanguageKeywords(
                Set.of("<=", "tot en met"),
                Set.of("<", "voor", "tot"),
                Set.of(">=", "sinds", "vanaf"),
                Set.of(">", "na"),
                Set.of("tot", "t/m", "tot en met","-"),
                Set.of(),
                Set.of("vandaag"),
                Set.of("gisteren"),
                Set.of("morgen"),
                Set.of("vorige", "laatste"),
                Map.of(
                        "jaar", ChronoUnit.YEARS,
                        "jaren", ChronoUnit.YEARS,
                        "kwartaal", ChronoUnit.MONTHS,
                        "kwartalen", ChronoUnit.MONTHS,
                        "maand", ChronoUnit.MONTHS,
                        "maanden", ChronoUnit.MONTHS,
                        "week", ChronoUnit.WEEKS,
                        "weken", ChronoUnit.WEEKS,
                        "dag", ChronoUnit.DAYS,
                        "dagen", ChronoUnit.DAYS
                ),
                Map.of(
                        "maandag", DayOfWeek.MONDAY,
                        "dinsdag", DayOfWeek.TUESDAY,
                        "woensdag", DayOfWeek.WEDNESDAY,
                        "donderdag", DayOfWeek.THURSDAY,
                        "vrijdag", DayOfWeek.FRIDAY,
                        "zaterdag", DayOfWeek.SATURDAY,
                        "zondag", DayOfWeek.SUNDAY
                )
        );

        public static LanguageKeywords combine(List<LanguageKeywords> keywordsList) {
            Map<String, DayOfWeek> combinedDaysOfWeek = new HashMap<>();
            keywordsList.forEach(k -> combinedDaysOfWeek.putAll(k.daysOfWeek()));
            Map<String, ChronoUnit> combinedChronoUnits = new HashMap<>();
            keywordsList.forEach(k -> combinedChronoUnits.putAll(k.chronoUnits()));

            return new LanguageKeywords(
                    keywordsList.stream().flatMap(k -> k.untilInclusive().stream()).collect(Collectors.toUnmodifiableSet()),
                    keywordsList.stream().flatMap(k -> k.untilExclusive().stream()).collect(Collectors.toUnmodifiableSet()),
                    keywordsList.stream().flatMap(k -> k.fromInclusive().stream()).collect(Collectors.toUnmodifiableSet()),
                    keywordsList.stream().flatMap(k -> k.fromExclusive().stream()).collect(Collectors.toUnmodifiableSet()),
                    keywordsList.stream().flatMap(k -> k.rangeConnectorsInclusive().stream()).collect(Collectors.toUnmodifiableSet()),
                    keywordsList.stream().flatMap(k -> k.rangeConnectorsExclusive().stream()).collect(Collectors.toUnmodifiableSet()),
                    keywordsList.stream().flatMap(k -> k.today().stream()).collect(Collectors.toUnmodifiableSet()),
                    keywordsList.stream().flatMap(k -> k.yesterday().stream()).collect(Collectors.toUnmodifiableSet()),
                    keywordsList.stream().flatMap(k -> k.tomorrow().stream()).collect(Collectors.toUnmodifiableSet()),
                    keywordsList.stream().flatMap(k -> k.last().stream()).collect(Collectors.toUnmodifiableSet()),
                    Map.copyOf(combinedChronoUnits),
                    Map.copyOf(combinedDaysOfWeek)
            );
        }
}
