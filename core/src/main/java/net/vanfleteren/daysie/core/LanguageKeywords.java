package net.vanfleteren.daysie.core;

import lombok.Builder;

import java.time.DayOfWeek;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Builder
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
        Set<String> dayBeforeYesterday,
        Set<String> dayAfterTomorrow,
        Set<String> last,
        Set<String> next,
        Set<String> current,
        Set<String> startOf,
        Set<String> endOf,
        Set<String> between,
        Set<String> and,
        Set<String> at,
        Set<String> now,
        Set<String> ago,
        Set<String> fromNow,
        Map<String, ChronoUnit> chronoUnits,
        Map<String, DayOfWeek> daysOfWeek
    ) {
        public static final LanguageKeywords ENGLISH = LanguageKeywords.builder()
                .untilInclusive(Set.of("<=", "until", "up to"))
                .untilExclusive(Set.of("<", "before"))
                .fromInclusive(Set.of(">=", "since", "from"))
                .fromExclusive(Set.of(">", "after"))
                .rangeConnectorsInclusive(Set.of("to","-"))
                .rangeConnectorsExclusive(Set.of())
                .today(Set.of("today"))
                .yesterday(Set.of("yesterday"))
                .tomorrow(Set.of("tomorrow"))
                .dayBeforeYesterday(Set.of("day before yesterday"))
                .dayAfterTomorrow(Set.of("day after tomorrow"))
                .last(Set.of("last", "previous", "past"))
                .next(Set.of("next"))
                .current(Set.of("this"))
                .startOf(Set.of("start of", "beginning of"))
                .endOf(Set.of("end of"))
                .between(Set.of("between"))
                .and(Set.of("and"))
                .at(Set.of("at"))
                .now(Set.of("now"))
                .ago(Set.of("ago"))
                .fromNow(Set.of("from now"))
                .chronoUnits(Map.ofEntries(
                        Map.entry("hour", ChronoUnit.HOURS),
                        Map.entry("hours", ChronoUnit.HOURS),
                        Map.entry("h", ChronoUnit.HOURS),
                        Map.entry("minute", ChronoUnit.MINUTES),
                        Map.entry("minutes", ChronoUnit.MINUTES),
                        Map.entry("m", ChronoUnit.MINUTES),
                        Map.entry("second", ChronoUnit.SECONDS),
                        Map.entry("seconds", ChronoUnit.SECONDS),
                        Map.entry("s", ChronoUnit.SECONDS),
                        Map.entry("year", ChronoUnit.YEARS),
                        Map.entry("years", ChronoUnit.YEARS),
                        Map.entry("y", ChronoUnit.YEARS),
                        Map.entry("quarter", ChronoUnit.MONTHS),
                        Map.entry("quarters", ChronoUnit.MONTHS),
                        Map.entry("month", ChronoUnit.MONTHS),
                        Map.entry("months", ChronoUnit.MONTHS),
                        Map.entry("week", ChronoUnit.WEEKS),
                        Map.entry("weeks", ChronoUnit.WEEKS),
                        Map.entry("w", ChronoUnit.WEEKS),
                        Map.entry("day", ChronoUnit.DAYS),
                        Map.entry("days", ChronoUnit.DAYS),
                        Map.entry("d", ChronoUnit.DAYS)
                ))
                .daysOfWeek(Map.ofEntries(
                        Map.entry("monday", DayOfWeek.MONDAY),
                        Map.entry("mondays", DayOfWeek.MONDAY),
                        Map.entry("tuesday", DayOfWeek.TUESDAY),
                        Map.entry("tuesdays", DayOfWeek.TUESDAY),
                        Map.entry("wednesday", DayOfWeek.WEDNESDAY),
                        Map.entry("wednesdays", DayOfWeek.WEDNESDAY),
                        Map.entry("thursday", DayOfWeek.THURSDAY),
                        Map.entry("thursdays", DayOfWeek.THURSDAY),
                        Map.entry("friday", DayOfWeek.FRIDAY),
                        Map.entry("fridays", DayOfWeek.FRIDAY),
                        Map.entry("saturday", DayOfWeek.SATURDAY),
                        Map.entry("saturdays", DayOfWeek.SATURDAY),
                        Map.entry("sunday", DayOfWeek.SUNDAY),
                        Map.entry("sundays", DayOfWeek.SUNDAY)
                ))
                .build();

        public static final LanguageKeywords DUTCH = LanguageKeywords.builder()
                .untilInclusive(Set.of("<=", "tot en met"))
                .untilExclusive(Set.of("<", "voor", "tot"))
                .fromInclusive(Set.of(">=", "sinds", "vanaf"))
                .fromExclusive(Set.of(">", "na"))
                .rangeConnectorsInclusive(Set.of("tot", "t/m", "tot en met","-"))
                .rangeConnectorsExclusive(Set.of())
                .today(Set.of("vandaag"))
                .yesterday(Set.of("gisteren"))
                .tomorrow(Set.of("morgen"))
                .dayBeforeYesterday(Set.of("eergisteren"))
                .dayAfterTomorrow(Set.of("overmorgen"))
                .last(Set.of("vorige", "laatste", "afgelopen", "voorbije"))
                .next(Set.of("volgende"))
                .current(Set.of("deze", "dit"))
                .startOf(Set.of("begin van"))
                .endOf(Set.of("einde van"))
                .between(Set.of("tussen"))
                .and(Set.of("en"))
                .at(Set.of("om"))
                .now(Set.of("nu"))
                .ago(Set.of("geleden", "terug"))
                .fromNow(Set.of("vanaf nu"))
                .chronoUnits(Map.ofEntries(
                        Map.entry("uur", ChronoUnit.HOURS),
                        Map.entry("uren", ChronoUnit.HOURS),
                        Map.entry("u", ChronoUnit.HOURS),
                        Map.entry("minuut", ChronoUnit.MINUTES),
                        Map.entry("minuten", ChronoUnit.MINUTES),
                        Map.entry("m", ChronoUnit.MINUTES),
                        Map.entry("seconde", ChronoUnit.SECONDS),
                        Map.entry("seconden", ChronoUnit.SECONDS),
                        Map.entry("s", ChronoUnit.SECONDS),
                        Map.entry("jaar", ChronoUnit.YEARS),
                        Map.entry("jaren", ChronoUnit.YEARS),
                        Map.entry("j", ChronoUnit.YEARS),
                        Map.entry("kwartaal", ChronoUnit.MONTHS),
                        Map.entry("kwartalen", ChronoUnit.MONTHS),
                        Map.entry("maand", ChronoUnit.MONTHS),
                        Map.entry("maanden", ChronoUnit.MONTHS),
                        Map.entry("week", ChronoUnit.WEEKS),
                        Map.entry("weken", ChronoUnit.WEEKS),
                        Map.entry("w", ChronoUnit.WEEKS),
                        Map.entry("dag", ChronoUnit.DAYS),
                        Map.entry("dagen", ChronoUnit.DAYS),
                        Map.entry("d", ChronoUnit.DAYS)
                ))
                .daysOfWeek(Map.ofEntries(
                        Map.entry("maandag", DayOfWeek.MONDAY),
                        Map.entry("maandagen", DayOfWeek.MONDAY),
                        Map.entry("dinsdag", DayOfWeek.TUESDAY),
                        Map.entry("dinsdagen", DayOfWeek.TUESDAY),
                        Map.entry("woensdag", DayOfWeek.WEDNESDAY),
                        Map.entry("woensdagen", DayOfWeek.WEDNESDAY),
                        Map.entry("donderdag", DayOfWeek.THURSDAY),
                        Map.entry("donderdagen", DayOfWeek.THURSDAY),
                        Map.entry("vrijdag", DayOfWeek.FRIDAY),
                        Map.entry("vrijdagen", DayOfWeek.FRIDAY),
                        Map.entry("zaterdag", DayOfWeek.SATURDAY),
                        Map.entry("zaterdagen", DayOfWeek.SATURDAY),
                        Map.entry("zondag", DayOfWeek.SUNDAY),
                        Map.entry("zondagen", DayOfWeek.SUNDAY)
                ))
                .build();

        public static LanguageKeywords combine(List<LanguageKeywords> keywordsList) {
            Function<Function<LanguageKeywords, Set<String>>, Set<String>> combineSets = extractor -> keywordsList.stream()
                    .flatMap(k -> extractor.apply(k).stream())
                    .collect(Collectors.toUnmodifiableSet());

            Map<String, DayOfWeek> combinedDaysOfWeek = new HashMap<>();
            keywordsList.forEach(k -> combinedDaysOfWeek.putAll(k.daysOfWeek()));
            Map<String, ChronoUnit> combinedChronoUnits = new HashMap<>();
            keywordsList.forEach(k -> combinedChronoUnits.putAll(k.chronoUnits()));

            return LanguageKeywords.builder()
                    .untilInclusive(combineSets.apply(LanguageKeywords::untilInclusive))
                    .untilExclusive(combineSets.apply(LanguageKeywords::untilExclusive))
                    .fromInclusive(combineSets.apply(LanguageKeywords::fromInclusive))
                    .fromExclusive(combineSets.apply(LanguageKeywords::fromExclusive))
                    .rangeConnectorsInclusive(combineSets.apply(LanguageKeywords::rangeConnectorsInclusive))
                    .rangeConnectorsExclusive(combineSets.apply(LanguageKeywords::rangeConnectorsExclusive))
                    .today(combineSets.apply(LanguageKeywords::today))
                    .yesterday(combineSets.apply(LanguageKeywords::yesterday))
                    .tomorrow(combineSets.apply(LanguageKeywords::tomorrow))
                    .dayBeforeYesterday(combineSets.apply(LanguageKeywords::dayBeforeYesterday))
                    .dayAfterTomorrow(combineSets.apply(LanguageKeywords::dayAfterTomorrow))
                    .last(combineSets.apply(LanguageKeywords::last))
                    .next(combineSets.apply(LanguageKeywords::next))
                    .current(combineSets.apply(LanguageKeywords::current))
                    .startOf(combineSets.apply(LanguageKeywords::startOf))
                    .endOf(combineSets.apply(LanguageKeywords::endOf))
                    .between(combineSets.apply(LanguageKeywords::between))
                    .and(combineSets.apply(LanguageKeywords::and))
                    .at(combineSets.apply(LanguageKeywords::at))
                    .now(combineSets.apply(LanguageKeywords::now))
                    .ago(combineSets.apply(LanguageKeywords::ago))
                    .fromNow(combineSets.apply(LanguageKeywords::fromNow))
                    .chronoUnits(Map.copyOf(combinedChronoUnits))
                    .daysOfWeek(Map.copyOf(combinedDaysOfWeek))
                    .build();
        }
}
