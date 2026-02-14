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
    private final Parser<DateValue> absoluteDateTimeParser;
    private final Parser<DateValue> dateOnlyParser;

    public DateValueParser() {
        this(LanguageKeywords.ENGLISH, Clock.systemDefaultZone());
    }

    public DateValueParser(LanguageKeywords keywords) {
        this(keywords, Clock.systemDefaultZone());
    }

    public DateValueParser(LanguageKeywords keywords, Clock clock) {
        this.dateOnlyParser = createDateOnlyParser(keywords, clock);
        this.absoluteDateTimeParser = createAbsoluteDateTimeParser(keywords, clock);

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

        Parser<String> rangeInclusive = toScanner(keywords.rangeConnectorsInclusive());
        Parser<String> rangeExclusive = toScanner(keywords.rangeConnectorsExclusive());
        Parser<String> rangeOp = Parsers.or(rangeInclusive, rangeExclusive);

        Parser<DateValue> absoluteRange = Parsers.sequence(
                absoluteDateTimeParser,
                Scanners.WHITESPACES.many(),
                rangeOp,
                Scanners.WHITESPACES.many(),
                absoluteDateTimeParser,
                (fromValue, s1, op, s2, untilValue) -> {
                    LocalDateTime from;
                    LocalDateTime until;
                    boolean isUntilInclusive;

                    if (fromValue instanceof DateValue.AbsoluteDate ad) {
                        from = ad.date();
                    } else if (fromValue instanceof DateValue.AbsoluteRange ar) {
                        from = ar.from();
                    } else {
                        throw new IllegalStateException("Unexpected DateValue type: " + fromValue.getClass());
                    }

                    if (untilValue instanceof DateValue.AbsoluteDate ad) {
                        until = ad.date();
                        isUntilInclusive = containsIgnoreCase(keywords.rangeConnectorsInclusive(), op);
                    } else if (untilValue instanceof DateValue.AbsoluteRange ar) {
                        if (containsIgnoreCase(keywords.rangeConnectorsInclusive(), op)) {
                            until = ar.until();
                            isUntilInclusive = ar.untilInclusive();
                        } else {
                            until = ar.from();
                            isUntilInclusive = false;
                        }
                    } else {
                        throw new IllegalStateException("Unexpected DateValue type: " + untilValue.getClass());
                    }

                    return new DateValue.AbsoluteRange(from, until, true, isUntilInclusive);
                }
        );

        Parser<DateValue> modifiedDateValueParser = Parsers.or(
                generalizedLastParser,
                generalizedNextParser,
                generalizedThisParser,
                absoluteRange,
                absoluteDateTimeParser
        );

        Parser<DateValue> startOfParser = Parsers.sequence(
                toScanner(keywords.startOf()),
                Scanners.WHITESPACES.atLeast(1),
                modifiedDateValueParser,
                (op, spaces, dateValue) -> {
                    if (dateValue instanceof DateValue.AbsoluteRange ar) {
                        return new DateValue.AbsoluteDate(ar.from(), true, ar.fromInclusive());
                    } else if (dateValue instanceof DateValue.AbsoluteDate ad) {
                        return ad;
                    } else {
                        throw new IllegalStateException("Unexpected DateValue type: " + dateValue.getClass());
                    }
                }
        );

        Parser<DateValue> endOfParser = Parsers.sequence(
                toScanner(keywords.endOf()),
                Scanners.WHITESPACES.atLeast(1),
                modifiedDateValueParser,
                (op, spaces, dateValue) -> {
                    if (dateValue instanceof DateValue.AbsoluteRange ar) {
                        return new DateValue.AbsoluteDate(ar.until(), true, ar.untilInclusive());
                    } else if (dateValue instanceof DateValue.AbsoluteDate ad) {
                        return ad;
                    } else {
                        throw new IllegalStateException("Unexpected DateValue type: " + dateValue.getClass());
                    }
                }
        );

        Parser<DateValue> finalAbsoluteDateTimeParser = Parsers.or(
                startOfParser,
                endOfParser,
                modifiedDateValueParser
        );

        Parser<String> untilInclusive = toScanner(keywords.untilInclusive());
        Parser<String> untilExclusive = toScanner(keywords.untilExclusive());
        Parser<String> untilOp = Parsers.or(untilInclusive, untilExclusive);

        Parser<DateValue> untilAbsoluteDate = Parsers.sequence(
                untilOp,
                Scanners.WHITESPACES.many(),
                finalAbsoluteDateTimeParser,
                (op, spaces, dateValue) -> {
                    LocalDateTime date;
                    boolean inclusive;
                    if (dateValue instanceof DateValue.AbsoluteDate ad) {
                        date = ad.date();
                        if (ad.isRangeBoundary()) {
                            if (containsIgnoreCase(keywords.untilExclusive(), op)) {
                                inclusive = false;
                            } else {
                                inclusive = ad.isInclusive();
                            }
                        } else {
                            inclusive = containsIgnoreCase(keywords.untilInclusive(), op);
                        }
                    } else if (dateValue instanceof DateValue.AbsoluteRange ar) {
                        if (containsIgnoreCase(keywords.untilInclusive(), op)) {
                            date = ar.until();
                            inclusive = ar.untilInclusive();
                        } else {
                            date = ar.from();
                            inclusive = false;
                        }
                    } else {
                        throw new IllegalStateException("Unexpected DateValue type: " + dateValue.getClass());
                    }
                    return new DateValue.UntilAbsoluteDate(date, inclusive);
                }
        );

        Parser<String> fromInclusive = toScanner(keywords.fromInclusive());
        Parser<String> fromExclusive = toScanner(keywords.fromExclusive());
        Parser<String> fromOp = Parsers.or(fromInclusive, fromExclusive);

        Parser<DateValue> fromAbsoluteDate = Parsers.or(
                Parsers.sequence(
                        fromExclusive,
                        Scanners.WHITESPACES.many(),
                        dateOnlyParser,
                        (op, spaces, dateValue) -> {
                            LocalDateTime date;
                            if (dateValue instanceof DateValue.AbsoluteRange ar) {
                                date = ar.until();
                            } else {
                                throw new IllegalStateException("Unexpected DateValue type for dateOnlyParser: " + dateValue.getClass());
                            }
                            return new DateValue.FromAbsoluteDate(date, true);
                        }
                ),
                Parsers.sequence(
                        fromOp,
                        Scanners.WHITESPACES.many(),
                        finalAbsoluteDateTimeParser,
                        (op, spaces, dateValue) -> {
                            LocalDateTime date;
                            boolean inclusive = containsIgnoreCase(keywords.fromInclusive(), op);
                            if (dateValue instanceof DateValue.AbsoluteDate ad) {
                                date = ad.date();
                                if (ad.isRangeBoundary()) {
                                    if (containsIgnoreCase(keywords.fromExclusive(), op)) {
                                        inclusive = false;
                                    } else if (containsIgnoreCase(keywords.fromInclusive(), op)) {
                                        inclusive = true;
                                    } else {
                                        inclusive = ad.isInclusive();
                                    }
                                } else {
                                    inclusive = containsIgnoreCase(keywords.fromInclusive(), op);
                                }
                            } else if (dateValue instanceof DateValue.AbsoluteRange ar) {
                                date = ar.from();
                            } else {
                                throw new IllegalStateException("Unexpected DateValue type: " + dateValue.getClass());
                            }
                            return new DateValue.FromAbsoluteDate(date, inclusive);
                        }
                )
        );

        this.dateValueParser = Parsers.or(
                finalAbsoluteDateTimeParser,
                untilAbsoluteDate,
                fromAbsoluteDate
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

    private static Parser<DateValue> createDateOnlyParser(LanguageKeywords keywords, Clock clock) {
        Parser<DateValue> relativeDate = Parsers.or(
                Stream.of(
                        keywords.today().stream().map(s -> Scanners.stringCaseInsensitive(s).map(ignored -> {
                            LocalDate today = LocalDate.now(clock);
                            return (DateValue) new DateValue.AbsoluteRange(today.atStartOfDay(), today.plusDays(1).atStartOfDay(), true, false);
                        })),
                        keywords.yesterday().stream().map(s -> Scanners.stringCaseInsensitive(s).map(ignored -> {
                            LocalDate yesterday = LocalDate.now(clock).minusDays(1);
                            return (DateValue) new DateValue.AbsoluteRange(yesterday.atStartOfDay(), yesterday.plusDays(1).atStartOfDay(), true, false);
                        })),
                        keywords.tomorrow().stream().map(s -> Scanners.stringCaseInsensitive(s).map(ignored -> {
                            LocalDate tomorrow = LocalDate.now(clock).plusDays(1);
                            return (DateValue) new DateValue.AbsoluteRange(tomorrow.atStartOfDay(), tomorrow.plusDays(1).atStartOfDay(), true, false);
                        })),
                        keywords.dayBeforeYesterday().stream().map(s -> Scanners.stringCaseInsensitive(s).map(ignored -> {
                            LocalDate date = LocalDate.now(clock).minusDays(2);
                            return (DateValue) new DateValue.AbsoluteRange(date.atStartOfDay(), date.plusDays(1).atStartOfDay(), true, false);
                        })),
                        keywords.dayAfterTomorrow().stream().map(s -> Scanners.stringCaseInsensitive(s).map(ignored -> {
                            LocalDate date = LocalDate.now(clock).plusDays(2);
                            return (DateValue) new DateValue.AbsoluteRange(date.atStartOfDay(), date.plusDays(1).atStartOfDay(), true, false);
                        })),
                        keywords.daysOfWeek().entrySet().stream().map(entry -> Scanners.stringCaseInsensitive(entry.getKey()).map(ignored -> {
                            LocalDate day = LocalDate.now(clock).with(TemporalAdjusters.previousOrSame(entry.getValue()));
                            return (DateValue) new DateValue.AbsoluteRange(day.atStartOfDay(), day.plusDays(1).atStartOfDay(), true, false);
                        }))
                ).flatMap(s -> s).toList()
        );

        return Parsers.or(
                DATE_ONLY.notFollowedBy(Scanners.WHITESPACES.many().next(Scanners.isChar(Character::isDigit)))
                        .map(date -> new DateValue.AbsoluteRange(date, date.plusDays(1), true, false)),
                YEAR_MONTH,
                relativeDate
        );
    }

    private static Parser<DateValue> createAbsoluteDateTimeParser(LanguageKeywords keywords, Clock clock) {
        Parser<DateValue> timeOnly = TIME.map(time -> {
            LocalDate today = LocalDate.now(clock);
            return new DateValue.AbsoluteDate(LocalDateTime.of(today, time), false, true);
        });

        return Parsers.longest(
                DATE_TIME.map(dt -> new DateValue.AbsoluteDate(dt, false, true)),
                createDateOnlyParser(keywords, clock),
                timeOnly
        );
    }

    private static final Parser<LocalDate> DATE = Patterns.regex("\\d{4}-\\d{2}-\\d{2}")
            .toScanner("date")
            .source()
            .map(LocalDate::parse);

    private static final Parser<LocalTime> TIME = Parsers.or(
            Patterns.regex("\\d{2}:\\d{2}:\\d{2}")
                    .toScanner("time-with-seconds")
                    .source()
                    .map(LocalTime::parse),
            Patterns.regex("\\d{2}:\\d{2}")
                    .toScanner("time-without-seconds")
                    .source()
                    .map(s -> LocalTime.parse(s + ":00"))
    );

    private static final Parser<LocalDateTime> DATE_TIME = Parsers.sequence(
            DATE,
            Parsers.or(Scanners.WHITESPACES.atLeast(1), Scanners.stringCaseInsensitive("T")),
            TIME,
            (date, separator, time) -> LocalDateTime.of(date, time)
    );

    private static final Parser<LocalDateTime> DATE_ONLY = DATE.map(LocalDate::atStartOfDay);

    private static final Parser<DateValue.AbsoluteRange> YEAR_MONTH = Patterns.regex("\\d{4}-\\d{2}(?!-\\d{2})")
            .toScanner("year-month")
            .source()
            .map(s -> {
                LocalDate start = LocalDate.parse(s + "-01");
                return new DateValue.AbsoluteRange(start.atStartOfDay(), start.plusMonths(1).atStartOfDay(), true, false);
            });

    public Parser<DateValue> absoluteDateTimeParser() {
        return absoluteDateTimeParser;
    }

    public Parser<DateValue> parser() {
        return dateValueParser;
    }

    public static final Parser<DateValue> DATE_VALUE_PARSER = new DateValueParser().parser();

}
