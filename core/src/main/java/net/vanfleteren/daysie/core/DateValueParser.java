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
    private final Parser<LocalDateTime> dateOnlyParser;

    public DateValueParser() {
        this(LanguageKeywords.ENGLISH, Clock.systemDefaultZone());
    }

    public DateValueParser(LanguageKeywords keywords) {
        this(keywords, Clock.systemDefaultZone());
    }

    public DateValueParser(LanguageKeywords keywords, Clock clock) {
        this.dateOnlyParser = createDateOnlyParser(keywords, clock);
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

        Parser<DateValue> fromAbsoluteDate = Parsers.or(
                Parsers.sequence(
                        fromExclusive,
                        Scanners.WHITESPACES.many(),
                        dateOnlyParser,
                        (op, spaces, date) -> new DateValue.FromAbsoluteDate(date.plusDays(1), true)
                ),
                Parsers.sequence(
                        fromOp,
                        Scanners.WHITESPACES.many(),
                        absoluteDateTimeParser,
                        (op, spaces, date) -> new DateValue.FromAbsoluteDate(date, containsIgnoreCase(keywords.fromInclusive(), op))
                )
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
        ).source().map(input -> calculateRelativeRangeFromInput(input, clock, keywords, RelativeDirection.LAST));

        Parser<DateValue> generalizedNextParser = Parsers.sequence(
                toScanner(keywords.next()),
                Scanners.WHITESPACES.atLeast(1),
                numberParser.optional(1),
                Scanners.WHITESPACES.many(),
                chronoUnitParser
        ).source().map(input -> calculateRelativeRangeFromInput(input, clock, keywords, RelativeDirection.NEXT));

        Parser<DateValue> generalizedThisParser = Parsers.sequence(
                toScanner(keywords.current()),
                Scanners.WHITESPACES.atLeast(1),
                numberParser.optional(1),
                Scanners.WHITESPACES.many(),
                chronoUnitParser
        ).source().map(input -> calculateRelativeRangeFromInput(input, clock, keywords, RelativeDirection.THIS));

        this.dateValueParser = Parsers.or(
                absoluteRange,
                generalizedLastParser,
                generalizedNextParser,
                generalizedThisParser,
                untilAbsoluteDate,
                fromAbsoluteDate,
                absoluteDateTimeParser.map(DateValue.AbsoluteDate::new)
        );
    }

    private enum RelativeDirection { LAST, NEXT, THIS }

    private DateValue calculateRelativeRangeFromInput(String input, Clock clock, LanguageKeywords keywords, RelativeDirection direction) {
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

        return switch (direction) {
            case LAST -> calculateLastRange(today, now, unit, amount, matchedUnitKey);
            case NEXT -> calculateNextRange(today, now, unit, amount, matchedUnitKey);
            case THIS -> calculateThisRange(today, now, unit, amount, matchedUnitKey);
        };
    }

    private DateValue calculateLastRange(LocalDate today, LocalDateTime now, ChronoUnit unit, int amount, String matchedUnitKey) {
        return switch (unit) {
            case MINUTES -> {
                LocalDateTime start = now.minusMinutes(amount);
                yield new DateValue.AbsoluteRange(start, now, true, false);
            }
            case HOURS -> {
                LocalDateTime start = now.minusHours(amount);
                yield new DateValue.AbsoluteRange(start, now, true, false);
            }
            case DAYS -> {
                LocalDate start = today.minusDays(amount);
                yield new DateValue.AbsoluteRange(start.atStartOfDay(), now, true, false);
            }
            case WEEKS -> {
                LocalDate startOfThisWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                LocalDate start = startOfThisWeek.minusWeeks(amount);
                yield new DateValue.AbsoluteRange(start.atStartOfDay(), startOfThisWeek.atStartOfDay(), true, false);
            }
            case MONTHS -> {
                if (isQuarter(matchedUnitKey)) {
                    LocalDate startOfThisQuarter = getStartOfQuarter(today);
                    LocalDate start = startOfThisQuarter.minusMonths(amount * 3L);
                    yield new DateValue.AbsoluteRange(start.atStartOfDay(), startOfThisQuarter.atStartOfDay(), true, false);
                } else {
                    LocalDate startOfThisMonth = today.with(TemporalAdjusters.firstDayOfMonth());
                    LocalDate start = startOfThisMonth.minusMonths(amount);
                    yield new DateValue.AbsoluteRange(start.atStartOfDay(), startOfThisMonth.atStartOfDay(), true, false);
                }
            }
            case YEARS -> {
                LocalDate startOfThisYear = today.with(TemporalAdjusters.firstDayOfYear());
                LocalDate start = startOfThisYear.minusYears(amount);
                yield new DateValue.AbsoluteRange(start.atStartOfDay(), startOfThisYear.atStartOfDay(), true, false);
            }
            default -> null;
        };
    }

    private DateValue calculateThisRange(LocalDate today, LocalDateTime now, ChronoUnit unit, int amount, String matchedUnitKey) {
        return switch (unit) {
            case MINUTES -> {
                LocalDateTime start = now.minusMinutes(amount - 1L).withNano(0).withSecond(0);
                yield new DateValue.AbsoluteRange(start, start.plusMinutes(1), true, false);
            }
            case HOURS -> {
                LocalDateTime start = now.minusHours(amount - 1L).withNano(0).withSecond(0).withMinute(0);
                yield new DateValue.AbsoluteRange(start, start.plusHours(1), true, false);
            }
            case DAYS -> {
                LocalDate start = today.minusDays(amount - 1L);
                yield new DateValue.AbsoluteRange(start.atStartOfDay(), today.plusDays(1).atStartOfDay(), true, false);
            }
            case WEEKS -> {
                LocalDate startOfThisWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                LocalDate start = startOfThisWeek.minusWeeks(amount - 1L);
                yield new DateValue.AbsoluteRange(start.atStartOfDay(), startOfThisWeek.plusWeeks(1).atStartOfDay(), true, false);
            }
            case MONTHS -> {
                if (isQuarter(matchedUnitKey)) {
                    LocalDate startOfThisQuarter = getStartOfQuarter(today);
                    LocalDate start = startOfThisQuarter.minusMonths((amount - 1L) * 3);
                    yield new DateValue.AbsoluteRange(start.atStartOfDay(), startOfThisQuarter.plusMonths(3).atStartOfDay(), true, false);
                } else {
                    LocalDate startOfThisMonth = today.with(TemporalAdjusters.firstDayOfMonth());
                    LocalDate start = startOfThisMonth.minusMonths(amount - 1L);
                    yield new DateValue.AbsoluteRange(start.atStartOfDay(), startOfThisMonth.plusMonths(1).atStartOfDay(), true, false);
                }
            }
            case YEARS -> {
                LocalDate startOfThisYear = today.with(TemporalAdjusters.firstDayOfYear());
                LocalDate start = startOfThisYear.minusYears(amount - 1L);
                yield new DateValue.AbsoluteRange(start.atStartOfDay(), startOfThisYear.plusYears(1).atStartOfDay(), true, false);
            }
            default -> null;
        };
    }

    private DateValue calculateNextRange(LocalDate today, LocalDateTime now, ChronoUnit unit, int amount, String matchedUnitKey) {
        return switch (unit) {
            case MINUTES -> {
                LocalDateTime end = now.plusMinutes(amount);
                yield new DateValue.AbsoluteRange(now, end, true, false);
            }
            case HOURS -> {
                LocalDateTime end = now.plusHours(amount);
                yield new DateValue.AbsoluteRange(now, end, true, false);
            }
            case DAYS -> {
                yield new DateValue.AbsoluteRange(now, today.plusDays(amount + 1L).atStartOfDay(), true, false);
            }
            case WEEKS -> {
                LocalDate startOfNextWeek = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
                yield new DateValue.AbsoluteRange(startOfNextWeek.atStartOfDay(), startOfNextWeek.plusWeeks(amount).atStartOfDay(), true, false);
            }
            case MONTHS -> {
                if (isQuarter(matchedUnitKey)) {
                    LocalDate startOfNextQuarter = getStartOfQuarter(today).plusMonths(3);
                    yield new DateValue.AbsoluteRange(startOfNextQuarter.atStartOfDay(), startOfNextQuarter.plusMonths(amount * 3L).atStartOfDay(), true, false);
                } else {
                    LocalDate startOfNextMonth = today.with(TemporalAdjusters.firstDayOfMonth()).plusMonths(1);
                    yield new DateValue.AbsoluteRange(startOfNextMonth.atStartOfDay(), startOfNextMonth.plusMonths(amount).atStartOfDay(), true, false);
                }
            }
            case YEARS -> {
                LocalDate startOfNextYear = today.with(TemporalAdjusters.firstDayOfYear()).plusYears(1);
                yield new DateValue.AbsoluteRange(startOfNextYear.atStartOfDay(), startOfNextYear.plusYears(amount).atStartOfDay(), true, false);
            }
            default -> null;
        };
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

    private static Parser<LocalDateTime> createDateOnlyParser(LanguageKeywords keywords, Clock clock) {
        Parser<LocalDateTime> relativeDate = Parsers.or(
                Stream.of(
                        keywords.today().stream().map(s -> Scanners.stringCaseInsensitive(s).map(ignored -> LocalDate.now(clock).atStartOfDay())),
                        keywords.yesterday().stream().map(s -> Scanners.stringCaseInsensitive(s).map(ignored -> LocalDate.now(clock).minusDays(1).atStartOfDay())),
                        keywords.tomorrow().stream().map(s -> Scanners.stringCaseInsensitive(s).map(ignored -> LocalDate.now(clock).plusDays(1).atStartOfDay())),
                        keywords.daysOfWeek().entrySet().stream().map(entry -> Scanners.stringCaseInsensitive(entry.getKey()).map(ignored -> LocalDate.now(clock).with(TemporalAdjusters.previousOrSame(entry.getValue())).atStartOfDay()))
                ).flatMap(s -> s).toList()
        );

        return Parsers.or(DATE_ONLY.notFollowedBy(Scanners.WHITESPACES.many().next(Scanners.isChar(Character::isDigit))), relativeDate);
    }

    private static Parser<LocalDateTime> createAbsoluteDateTimeParser(LanguageKeywords keywords, Clock clock) {
        return Parsers.longest(DATE_TIME, createDateOnlyParser(keywords, clock));
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
