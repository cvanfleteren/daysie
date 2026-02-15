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
        Set<String> firstDayOf,
        Set<String> lastDayOf,
        Set<String> between,
        Set<String> and,
        Set<String> at,
        Set<String> now,
        Set<String> ago,
        Set<String> fromNow,
        Set<String> am,
        Set<String> pm,
        Set<String> in,
        Set<String> quarters,
        Map<String, ChronoUnit> chronoUnits,
        Map<String, DayOfWeek> daysOfWeek
    ) {
        public static final LanguageKeywords ENGLISH = LanguageKeywords.builder()
                .ago(Set.of("ago"))
                .am(Set.of("AM"))
                .and(Set.of("and"))
                .at(Set.of("at"))
                .between(Set.of("between"))
                .current(Set.of("this"))
                .dayAfterTomorrow(Set.of("day after tomorrow"))
                .dayBeforeYesterday(Set.of("day before yesterday"))
                .endOf(Set.of("end of"))
                .firstDayOf(Set.of("first day of"))
                .fromExclusive(Set.of(">", "after"))
                .fromInclusive(Set.of(">=", "since", "from"))
                .fromNow(Set.of("from now"))
                .in(Set.of("in"))
                .last(Set.of("last", "previous", "past"))
                .lastDayOf(Set.of("last day of"))
                .next(Set.of("next"))
                .now(Set.of("now"))
                .pm(Set.of("PM"))
                .quarters(Set.of("quarter", "quarters"))
                .rangeConnectorsExclusive(Set.of())
                .rangeConnectorsInclusive(Set.of("to","-"))
                .startOf(Set.of("start of", "beginning of"))
                .today(Set.of("today"))
                .tomorrow(Set.of("tomorrow"))
                .untilExclusive(Set.of("<", "before"))
                .untilInclusive(Set.of("<=", "until", "up to"))
                .yesterday(Set.of("yesterday"))
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
                .ago(Set.of("geleden", "terug"))
                .am(Set.of("AM"))
                .and(Set.of("en"))
                .at(Set.of("om"))
                .between(Set.of("tussen"))
                .current(Set.of("deze", "dit"))
                .dayAfterTomorrow(Set.of("overmorgen"))
                .dayBeforeYesterday(Set.of("eergisteren"))
                .endOf(Set.of("einde van"))
                .firstDayOf(Set.of("eerste dag van"))
                .fromExclusive(Set.of(">", "na"))
                .fromInclusive(Set.of(">=", "sinds", "vanaf"))
                .fromNow(Set.of("vanaf nu"))
                .in(Set.of("over"))
                .last(Set.of("vorige", "laatste", "afgelopen", "voorbije"))
                .lastDayOf(Set.of("laatste dag van"))
                .next(Set.of("volgende"))
                .now(Set.of("nu"))
                .pm(Set.of("PM"))
                .quarters(Set.of("kwartaal", "kwartalen"))
                .rangeConnectorsExclusive(Set.of())
                .rangeConnectorsInclusive(Set.of("tot", "t/m", "tot en met","-"))
                .startOf(Set.of("begin van"))
                .today(Set.of("vandaag"))
                .tomorrow(Set.of("morgen"))
                .untilExclusive(Set.of("<", "voor", "tot"))
                .untilInclusive(Set.of("<=", "tot en met"))
                .yesterday(Set.of("gisteren"))
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
                    .ago(combineSets.apply(LanguageKeywords::ago))
                    .am(combineSets.apply(LanguageKeywords::am))
                    .and(combineSets.apply(LanguageKeywords::and))
                    .at(combineSets.apply(LanguageKeywords::at))
                    .between(combineSets.apply(LanguageKeywords::between))
                    .chronoUnits(Map.copyOf(combinedChronoUnits))
                    .current(combineSets.apply(LanguageKeywords::current))
                    .dayAfterTomorrow(combineSets.apply(LanguageKeywords::dayAfterTomorrow))
                    .dayBeforeYesterday(combineSets.apply(LanguageKeywords::dayBeforeYesterday))
                    .daysOfWeek(Map.copyOf(combinedDaysOfWeek))
                    .endOf(combineSets.apply(LanguageKeywords::endOf))
                    .firstDayOf(combineSets.apply(LanguageKeywords::firstDayOf))
                    .fromExclusive(combineSets.apply(LanguageKeywords::fromExclusive))
                    .fromInclusive(combineSets.apply(LanguageKeywords::fromInclusive))
                    .fromNow(combineSets.apply(LanguageKeywords::fromNow))
                    .in(combineSets.apply(LanguageKeywords::in))
                    .last(combineSets.apply(LanguageKeywords::last))
                    .lastDayOf(combineSets.apply(LanguageKeywords::lastDayOf))
                    .next(combineSets.apply(LanguageKeywords::next))
                    .now(combineSets.apply(LanguageKeywords::now))
                    .pm(combineSets.apply(LanguageKeywords::pm))
                    .quarters(combineSets.apply(LanguageKeywords::quarters))
                    .rangeConnectorsExclusive(combineSets.apply(LanguageKeywords::rangeConnectorsExclusive))
                    .rangeConnectorsInclusive(combineSets.apply(LanguageKeywords::rangeConnectorsInclusive))
                    .startOf(combineSets.apply(LanguageKeywords::startOf))
                    .today(combineSets.apply(LanguageKeywords::today))
                    .tomorrow(combineSets.apply(LanguageKeywords::tomorrow))
                    .untilExclusive(combineSets.apply(LanguageKeywords::untilExclusive))
                    .untilInclusive(combineSets.apply(LanguageKeywords::untilInclusive))
                    .yesterday(combineSets.apply(LanguageKeywords::yesterday))
                    .build();
        }
}
