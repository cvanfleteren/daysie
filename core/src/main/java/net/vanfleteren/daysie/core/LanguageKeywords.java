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
        Set<String> next,
        Set<String> current,
        Set<String> startOf,
        Set<String> endOf,
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
                Set.of("last", "previous", "past"),
                Set.of("next"),
                Set.of("this"),
                Set.of("start of", "beginning of"),
                Set.of("end of"),
                Map.ofEntries(
                        Map.entry("hour", ChronoUnit.HOURS),
                        Map.entry("hours", ChronoUnit.HOURS),
                        Map.entry("minute", ChronoUnit.MINUTES),
                        Map.entry("minutes", ChronoUnit.MINUTES),
                        Map.entry("year", ChronoUnit.YEARS),
                        Map.entry("years", ChronoUnit.YEARS),
                        Map.entry("quarter", ChronoUnit.MONTHS),
                        Map.entry("quarters", ChronoUnit.MONTHS),
                        Map.entry("month", ChronoUnit.MONTHS),
                        Map.entry("months", ChronoUnit.MONTHS),
                        Map.entry("week", ChronoUnit.WEEKS),
                        Map.entry("weeks", ChronoUnit.WEEKS),
                        Map.entry("day", ChronoUnit.DAYS),
                        Map.entry("days", ChronoUnit.DAYS)
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
                Set.of("vorige", "laatste", "afgelopen"),
                Set.of("volgende"),
                Set.of("deze", "dit"),
                Set.of("begin van"),
                Set.of("einde van"),
                Map.ofEntries(
                        Map.entry("uur", ChronoUnit.HOURS),
                        Map.entry("uren", ChronoUnit.HOURS),
                        Map.entry("minuut", ChronoUnit.MINUTES),
                        Map.entry("minuten", ChronoUnit.MINUTES),
                        Map.entry("jaar", ChronoUnit.YEARS),
                        Map.entry("jaren", ChronoUnit.YEARS),
                        Map.entry("kwartaal", ChronoUnit.MONTHS),
                        Map.entry("kwartalen", ChronoUnit.MONTHS),
                        Map.entry("maand", ChronoUnit.MONTHS),
                        Map.entry("maanden", ChronoUnit.MONTHS),
                        Map.entry("week", ChronoUnit.WEEKS),
                        Map.entry("weken", ChronoUnit.WEEKS),
                        Map.entry("dag", ChronoUnit.DAYS),
                        Map.entry("dagen", ChronoUnit.DAYS)
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
                    keywordsList.stream().flatMap(k -> k.next().stream()).collect(Collectors.toUnmodifiableSet()),
                    keywordsList.stream().flatMap(k -> k.current().stream()).collect(Collectors.toUnmodifiableSet()),
                    keywordsList.stream().flatMap(k -> k.startOf().stream()).collect(Collectors.toUnmodifiableSet()),
                    keywordsList.stream().flatMap(k -> k.endOf().stream()).collect(Collectors.toUnmodifiableSet()),
                    Map.copyOf(combinedChronoUnits),
                    Map.copyOf(combinedDaysOfWeek)
            );
        }
}
