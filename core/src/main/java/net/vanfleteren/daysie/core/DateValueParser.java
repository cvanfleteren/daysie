package net.vanfleteren.daysie.core;

import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.jparsec.Scanners;
import org.jparsec.pattern.Patterns;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class DateValueParser {
    private static final Comparator<String> BY_LENGTH_DESC = Comparator.comparingInt(String::length).reversed();

    private final Parser<DateValue> dateValueParser;
    private final Parser<LocalDateTime> absoluteDateTimeParser;

    public DateValueParser() {
        this(LanguageKeywords.ENGLISH, Clock.systemDefaultZone());
    }

    public DateValueParser(LanguageKeywords keywords) {
        this(keywords, Clock.systemDefaultZone());
    }

    public DateValueParser(LanguageKeywords keywords, Clock clock) {
        this.absoluteDateTimeParser = createAbsoluteDateTimeParser(keywords, clock);
        Parser<String> untilInclusive = toScanner(keywords.untilInclusive());
        Parser<String> untilExclusive = toScanner(keywords.untilExclusive());
        Parser<String> untilOp = Parsers.or(untilInclusive, untilExclusive);

        Parser<DateValue> untilAbsoluteDate = Parsers.sequence(
                untilOp,
                Scanners.WHITESPACES.many(),
                absoluteDateTimeParser,
                (op, spaces, date) -> new DateValue.UntilAbsoluteDate(date, containsIgnoreCase(keywords.untilInclusive(), op))
        );

        Parser<String> fromInclusive = toScanner(keywords.fromInclusive());
        Parser<String> fromExclusive = toScanner(keywords.fromExclusive());
        Parser<String> fromOp = Parsers.or(fromInclusive, fromExclusive);

        Parser<DateValue> fromAbsoluteDate = Parsers.sequence(
                fromOp,
                Scanners.WHITESPACES.many(),
                absoluteDateTimeParser,
                (op, spaces, date) -> new DateValue.FromAbsoluteDate(date, containsIgnoreCase(keywords.fromInclusive(), op))
        );

        Parser<String> rangeInclusive = toScanner(keywords.rangeConnectorsInclusive());
        Parser<String> rangeExclusive = toScanner(keywords.rangeConnectorsExclusive());
        Parser<String> rangeOp = Parsers.or(rangeInclusive, rangeExclusive);

        Parser<DateValue> absoluteRange = Parsers.sequence(
                absoluteDateTimeParser,
                Scanners.WHITESPACES.many(),
                rangeOp,
                Scanners.WHITESPACES.many(),
                absoluteDateTimeParser,
                (from, s1, op, s2, until) -> new DateValue.AbsoluteRange(from, until, true, containsIgnoreCase(keywords.rangeConnectorsInclusive(), op))
        );

        Parser<Integer> numberParser = Scanners.INTEGER.map(Integer::parseInt);

        Parser<ChronoUnit> chronoUnitParser = Parsers.or(
                keywords.chronoUnits().entrySet().stream()
                        .sorted((e1, e2) -> e2.getKey().length() - e1.getKey().length())
                        .map(entry -> Scanners.stringCaseInsensitive(entry.getKey()).map(ignored -> entry.getValue()))
                        .toList()
        );

        Parser<DateValue> generalizedLastParser = Parsers.sequence(
                toScanner(keywords.last()),
                Scanners.WHITESPACES.atLeast(1),
                numberParser.optional(1),
                Scanners.WHITESPACES.many(),
                chronoUnitParser
        ).source().map(input -> calculateRelativeRangeFromInput(input, clock, keywords, true));

        Parser<DateValue> generalizedNextParser = Parsers.sequence(
                toScanner(keywords.next()),
                Scanners.WHITESPACES.atLeast(1),
                numberParser.optional(1),
                Scanners.WHITESPACES.many(),
                chronoUnitParser
        ).source().map(input -> calculateRelativeRangeFromInput(input, clock, keywords, false));

        this.dateValueParser = Parsers.or(
                absoluteRange,
                generalizedLastParser,
                generalizedNextParser,
                untilAbsoluteDate,
                fromAbsoluteDate,
                absoluteDateTimeParser.map(DateValue.AbsoluteDate::new)
        );
    }

    private DateValue calculateRelativeRangeFromInput(String input, Clock clock, LanguageKeywords keywords, boolean isPast) {
        String lowerInput = input.toLowerCase();
        int amount = 1;
        // Try to find a number
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("\\d+").matcher(lowerInput);
        if (matcher.find()) {
            amount = Integer.parseInt(matcher.group());
        }

        ChronoUnit unit = null;
        String matchedUnitKey = "";
        for (Map.Entry<String, ChronoUnit> entry : keywords.chronoUnits().entrySet()) {
            String key = entry.getKey().toLowerCase();
            if (lowerInput.contains(key)) {
                if (key.length() > matchedUnitKey.length()) {
                    unit = entry.getValue();
                    matchedUnitKey = key;
                }
            }
        }

        LocalDate today = LocalDate.now(clock);
        LocalDateTime now = LocalDateTime.now(clock);

        if (isPast) {
            return calculateLastRange(today, now, unit, amount, matchedUnitKey);
        } else {
            return calculateNextRange(today, now, unit, amount, matchedUnitKey);
        }
    }

    private DateValue calculateLastRange(LocalDate today, LocalDateTime now, ChronoUnit unit, int amount, String matchedUnitKey) {
        if (unit == ChronoUnit.MINUTES) {
            LocalDateTime start = now.minusMinutes(amount);
            return new DateValue.AbsoluteRange(start, now, true, true);
        } else if (unit == ChronoUnit.HOURS) {
            LocalDateTime start = now.minusHours(amount);
            return new DateValue.AbsoluteRange(start, now, true, true);
        } else if (unit == ChronoUnit.DAYS) {
            LocalDate start = today.minusDays(amount);
            return new DateValue.AbsoluteRange(start.atStartOfDay(), now, true, true);
        } else if (unit == ChronoUnit.WEEKS) {
            LocalDate startOfThisWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate start = startOfThisWeek.minusWeeks(amount);
            LocalDate end = startOfThisWeek.minusDays(1);
            return new DateValue.AbsoluteRange(start.atStartOfDay(), end.atStartOfDay(), true, true);
        } else if (unit == ChronoUnit.MONTHS) {
            if (isQuarter(matchedUnitKey)) {
                LocalDate startOfThisQuarter = getStartOfQuarter(today);
                LocalDate start = startOfThisQuarter.minusMonths(amount * 3L);
                LocalDate end = startOfThisQuarter.minusDays(1);
                return new DateValue.AbsoluteRange(start.atStartOfDay(), end.atStartOfDay(), true, true);
            } else {
                LocalDate startOfThisMonth = today.with(TemporalAdjusters.firstDayOfMonth());
                LocalDate start = startOfThisMonth.minusMonths(amount);
                LocalDate end = startOfThisMonth.minusDays(1);
                return new DateValue.AbsoluteRange(start.atStartOfDay(), end.atStartOfDay(), true, true);
            }
        } else if (unit == ChronoUnit.YEARS) {
            LocalDate startOfThisYear = today.with(TemporalAdjusters.firstDayOfYear());
            LocalDate start = startOfThisYear.minusYears(amount);
            LocalDate end = startOfThisYear.minusDays(1);
            return new DateValue.AbsoluteRange(start.atStartOfDay(), end.atStartOfDay(), true, true);
        }
        return null;
    }

    private DateValue calculateNextRange(LocalDate today, LocalDateTime now, ChronoUnit unit, int amount, String matchedUnitKey) {
        if (unit == ChronoUnit.MINUTES) {
            LocalDateTime end = now.plusMinutes(amount);
            return new DateValue.AbsoluteRange(now, end, true, true);
        } else if (unit == ChronoUnit.HOURS) {
            LocalDateTime end = now.plusHours(amount);
            return new DateValue.AbsoluteRange(now, end, true, true);
        } else if (unit == ChronoUnit.DAYS) {
            LocalDate end = today.plusDays(amount);
            return new DateValue.AbsoluteRange(now, end.atTime(LocalTime.MAX), true, true);
        } else if (unit == ChronoUnit.WEEKS) {
            LocalDate startOfNextWeek = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
            LocalDate start = startOfNextWeek;
            LocalDate end = startOfNextWeek.plusWeeks(amount).minusDays(1);
            return new DateValue.AbsoluteRange(start.atStartOfDay(), end.atStartOfDay(), true, true);
        } else if (unit == ChronoUnit.MONTHS) {
            if (isQuarter(matchedUnitKey)) {
                LocalDate startOfNextQuarter = getStartOfQuarter(today).plusMonths(3);
                LocalDate end = startOfNextQuarter.plusMonths(amount * 3L).minusDays(1);
                return new DateValue.AbsoluteRange(startOfNextQuarter.atStartOfDay(), end.atStartOfDay(), true, true);
            } else {
                LocalDate startOfNextMonth = today.with(TemporalAdjusters.firstDayOfMonth()).plusMonths(1);
                LocalDate end = startOfNextMonth.plusMonths(amount).minusDays(1);
                return new DateValue.AbsoluteRange(startOfNextMonth.atStartOfDay(), end.atStartOfDay(), true, true);
            }
        } else if (unit == ChronoUnit.YEARS) {
            LocalDate startOfNextYear = today.with(TemporalAdjusters.firstDayOfYear()).plusYears(1);
            LocalDate end = startOfNextYear.plusYears(amount).minusDays(1);
            return new DateValue.AbsoluteRange(startOfNextYear.atStartOfDay(), end.atStartOfDay(), true, true);
        }
        return null;
    }

    private boolean isQuarter(String matchedUnitKey) {
        return matchedUnitKey.equalsIgnoreCase("quarter") || matchedUnitKey.equalsIgnoreCase("quarters")
                || matchedUnitKey.equalsIgnoreCase("kwartaal") || matchedUnitKey.equalsIgnoreCase("kwartalen");
    }

    private LocalDate getStartOfQuarter(LocalDate date) {
        int currentMonth = date.getMonthValue();
        int startMonthOfQuarter = ((currentMonth - 1) / 3) * 3 + 1;
        return date.withMonth(startMonthOfQuarter).withDayOfMonth(1);
    }

    private static boolean containsIgnoreCase(Set<String> set, String value) {
        return set.stream().anyMatch(s -> s.equalsIgnoreCase(value));
    }

    private static Parser<String> toScanner(Set<String> keywords) {
        return Parsers.or(keywords.stream().sorted(BY_LENGTH_DESC).map(Scanners::stringCaseInsensitive).toList()).source();
    }

    private static Parser<LocalDateTime> createAbsoluteDateTimeParser(LanguageKeywords keywords, Clock clock) {
        Parser<LocalDateTime> relativeDate = Parsers.or(
                Stream.of(
                        keywords.today().stream().map(s -> Scanners.stringCaseInsensitive(s).map(ignored -> LocalDate.now(clock).atStartOfDay())),
                        keywords.yesterday().stream().map(s -> Scanners.stringCaseInsensitive(s).map(ignored -> LocalDate.now(clock).minusDays(1).atStartOfDay())),
                        keywords.tomorrow().stream().map(s -> Scanners.stringCaseInsensitive(s).map(ignored -> LocalDate.now(clock).plusDays(1).atStartOfDay())),
                        keywords.daysOfWeek().entrySet().stream().map(entry -> Scanners.stringCaseInsensitive(entry.getKey()).map(ignored -> LocalDate.now(clock).with(TemporalAdjusters.previousOrSame(entry.getValue())).atStartOfDay()))
                ).flatMap(s -> s).toList()
        );

        return Parsers.longest(DATE_TIME, DATE_ONLY, relativeDate);
    }

    private static final Parser<LocalDate> DATE = Patterns.regex("\\d{4}-\\d{2}-\\d{2}")
            .toScanner("date")
            .source()
            .map(LocalDate::parse);

    private static final Parser<LocalTime> TIME = Patterns.regex("\\d{2}:\\d{2}:\\d{2}")
            .toScanner("time")
            .source()
            .map(LocalTime::parse);

    private static final Parser<LocalDateTime> DATE_TIME = Parsers.sequence(
            DATE,
            Parsers.or(Scanners.WHITESPACES.atLeast(1), Scanners.stringCaseInsensitive("T")),
            TIME,
            (date, separator, time) -> LocalDateTime.of(date, time)
    );

    private static final Parser<LocalDateTime> DATE_ONLY = DATE.map(LocalDate::atStartOfDay);

    public Parser<LocalDateTime> absoluteDateTimeParser() {
        return absoluteDateTimeParser;
    }

    public Parser<DateValue> parser() {
        return dateValueParser;
    }

    public static final Parser<DateValue> DATE_VALUE_PARSER = new DateValueParser().parser();

}
