package net.vanfleteren.daysie.core;

import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.jparsec.Scanners;
import org.jparsec.pattern.Patterns;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Stream;

public class DateValueParser {
    private static final Comparator<String> BY_LENGTH_DESC = Comparator.comparingInt(String::length).reversed();

    private final Parser<DateValue> dateValueParser;
    private final Parser<DateValue> absoluteDateTimeParser;

    private record ChronoUnitInfo(ChronoUnit unit, boolean isQuarter) {}

    public DateValueParser() {
        this(LanguageKeywords.ENGLISH, Clock.systemDefaultZone());
    }

    public DateValueParser(LanguageKeywords keywords) {
        this(keywords, Clock.systemDefaultZone());
    }

    public DateValueParser(LanguageKeywords keywords, Clock clock) {
        Parser<LocalTime> timeParser = createTimeParser(keywords);
        this.absoluteDateTimeParser = createAbsoluteDateTimeParser(keywords, clock, timeParser);

        Parser<Integer> numberParser = Scanners.INTEGER.map(Integer::parseInt);
        Parser<ChronoUnitInfo> chronoUnitParser = createChronoUnitParser(keywords);
        Parser<DateValue> relativeDateParser = createRelativeDateParser(keywords, clock);

        Parser<DateValue> generalizedLastParser = createGeneralizedLastParser(keywords, clock, chronoUnitParser, numberParser);
        Parser<DateValue> generalizedNextParser = createGeneralizedNextParser(keywords, clock, chronoUnitParser, numberParser);
        Parser<DateValue> generalizedThisParser = createGeneralizedThisParser(keywords, clock, chronoUnitParser, numberParser);

        Parser<DateValue> relativePoint = createRelativePointParser(keywords, clock, chronoUnitParser, numberParser);
        Parser<DateValue> relativePointWithTime = createRelativePointWithTimeParser(keywords, relativePoint, timeParser);

        Parser<String> rangeOp = createRangeOp(keywords);

        Parser.Reference<DateValue> absoluteRangeRef = Parser.newReference();
        Parser<DateValue> absoluteRange = absoluteRangeRef.lazy();

        Parser.Reference<DateValue> finalAbsoluteDateTimeParserRef = Parser.newReference();
        Parser<DateValue> finalAbsoluteDateTimeParser = finalAbsoluteDateTimeParserRef.lazy();

        absoluteRangeRef.set(createRangeParser(keywords, finalAbsoluteDateTimeParser, rangeOp));

        Parser<DateValue> baseModifierParser = Parsers.or(
                generalizedLastParser,
                generalizedNextParser,
                generalizedThisParser,
                relativeDateParser,
                absoluteDateTimeParser
        );

        Parser<DateValue> startOfParser = createStartOfParser(keywords, baseModifierParser);
        Parser<DateValue> endOfParser = createEndOfParser(keywords, baseModifierParser);
        Parser<DateValue> firstDayOfParser = createFirstDayOfParser(keywords, baseModifierParser);
        Parser<DateValue> lastDayOfParser = createLastDayOfParser(keywords, baseModifierParser);
        Parser<DateValue> betweenParser = createBetweenParser(keywords, finalAbsoluteDateTimeParser);

        finalAbsoluteDateTimeParserRef.set(Parsers.longest(
                startOfParser,
                endOfParser,
                firstDayOfParser,
                lastDayOfParser,
                betweenParser,
                relativePointWithTime,
                relativePoint,
                relativeDateParser,
                generalizedLastParser,
                generalizedNextParser,
                generalizedThisParser,
                absoluteDateTimeParser
        ));

        Parser<DateValue> untilAbsoluteDate = createUntilParser(keywords, finalAbsoluteDateTimeParser);
        Parser<DateValue> fromAbsoluteDate = createFromParser(keywords, createDateOnlyParser(keywords, clock), finalAbsoluteDateTimeParser);

        this.dateValueParser = Parsers.longest(
                absoluteRange,
                untilAbsoluteDate,
                fromAbsoluteDate,
                finalAbsoluteDateTimeParser
        ).followedBy(Scanners.WHITESPACES.many());
    }

    private Parser<ChronoUnitInfo> createChronoUnitParser(LanguageKeywords keywords) {
        return Parsers.or(
                keywords.chronoUnits().entrySet().stream()
                        .sorted((e1, e2) -> e2.getKey().length() - e1.getKey().length())
                        .map(entry -> toScanner(Set.of(entry.getKey())).map(ignored -> new ChronoUnitInfo(entry.getValue(), containsIgnoreCase(keywords.quarters(), entry.getKey()))))
                        .toList()
        );
    }

    private Parser<DateValue> createGeneralizedLastParser(LanguageKeywords keywords, Clock clock, Parser<ChronoUnitInfo> chronoUnitParser, Parser<Integer> numberParser) {
        return Parsers.sequence(
                toScanner(keywords.last()),
                Scanners.WHITESPACES.atLeast(1),
                Parsers.or(
                        Parsers.sequence(numberParser, Scanners.WHITESPACES.many(), chronoUnitParser, (amount, s, unitInfo) -> new Object[]{amount, unitInfo}),
                        chronoUnitParser.map(unitInfo -> new Object[]{1, unitInfo})
                ),
                (op, s1, info) -> {
                    int amount = (Integer) info[0];
                    ChronoUnitInfo unitInfo = (ChronoUnitInfo) info[1];
                    LocalDateTime now = LocalDateTime.now(clock);
                    return DateCalculator.calculateLastRange(now, unitInfo.unit(), amount, unitInfo.isQuarter());
                }
        ).notFollowedBy(Scanners.WHITESPACES.many().next(toScanner(keywords.at()).optional().next(Scanners.WHITESPACES.many()).next(TIME)));
    }

    private Parser<DateValue> createGeneralizedNextParser(LanguageKeywords keywords, Clock clock, Parser<ChronoUnitInfo> chronoUnitParser, Parser<Integer> numberParser) {
        return Parsers.sequence(
                toScanner(keywords.next()),
                Scanners.WHITESPACES.atLeast(1),
                Parsers.or(
                        Parsers.sequence(numberParser, Scanners.WHITESPACES.many(), chronoUnitParser, (amount, s, unitInfo) -> new Object[]{amount, unitInfo}),
                        chronoUnitParser.map(unitInfo -> new Object[]{1, unitInfo})
                ),
                (op, s1, info) -> {
                    int amount = (Integer) info[0];
                    ChronoUnitInfo unitInfo = (ChronoUnitInfo) info[1];
                    LocalDateTime now = LocalDateTime.now(clock);
                    return DateCalculator.calculateNextRange(now, unitInfo.unit(), amount, unitInfo.isQuarter());
                }
        ).notFollowedBy(Scanners.WHITESPACES.many().next(toScanner(keywords.at()).optional().next(Scanners.WHITESPACES.many()).next(TIME)));
    }

    private Parser<DateValue> createGeneralizedThisParser(LanguageKeywords keywords, Clock clock, Parser<ChronoUnitInfo> chronoUnitParser, Parser<Integer> numberParser) {
        return Parsers.sequence(
                toScanner(keywords.current()),
                Scanners.WHITESPACES.atLeast(1),
                Parsers.or(
                        Parsers.sequence(numberParser, Scanners.WHITESPACES.many(), chronoUnitParser, (amount, s, unitInfo) -> new Object[]{amount, unitInfo}),
                        chronoUnitParser.map(unitInfo -> new Object[]{1, unitInfo})
                ),
                (op, s1, info) -> {
                    int amount = (Integer) info[0];
                    ChronoUnitInfo unitInfo = (ChronoUnitInfo) info[1];
                    LocalDateTime now = LocalDateTime.now(clock);
                    return DateCalculator.calculateThisRange(now, unitInfo.unit(), amount, unitInfo.isQuarter());
                }
        ).notFollowedBy(Scanners.WHITESPACES.many().next(toScanner(keywords.at()).optional().next(Scanners.WHITESPACES.many()).next(TIME)));
    }

    private Parser<DateValue> createRelativePointParser(LanguageKeywords keywords, Clock clock, Parser<ChronoUnitInfo> chronoUnitParser, Parser<Integer> numberParser) {
        Parser<DayOfWeek> dayOfWeekParser = Parsers.or(
                keywords.daysOfWeek().entrySet().stream()
                        .sorted((e1, e2) -> e2.getKey().length() - e1.getKey().length())
                        .map(entry -> Scanners.stringCaseInsensitive(entry.getKey()).source().map(ignored -> entry.getValue()))
                        .toList()
        );

        Parser<DateValue> dayOfWeekAgoParser = Parsers.sequence(
                numberParser.optional(1),
                Scanners.WHITESPACES.many(),
                dayOfWeekParser,
                Scanners.WHITESPACES.many(),
                toScanner(keywords.ago()),
                (amount, s1, dayOfWeek, s2, op) -> {
                    LocalDateTime now = LocalDateTime.now(clock);
                    return DateCalculator.calculateDayOfWeekAgo(now, dayOfWeek, amount);
                }
        );

        Parser<DateValue> dayOfWeekFromNowParser = Parsers.sequence(
                numberParser.optional(1),
                Scanners.WHITESPACES.many(),
                dayOfWeekParser,
                Scanners.WHITESPACES.many(),
                toScanner(keywords.fromNow()),
                (amount, s1, dayOfWeek, s2, op) -> {
                    LocalDateTime now = LocalDateTime.now(clock);
                    return DateCalculator.calculateDayOfWeekFromNow(now, dayOfWeek, amount);
                }
        );

        Parser<DateValue> agoParser = Parsers.sequence(
                numberParser.optional(1),
                Scanners.WHITESPACES.many(),
                chronoUnitParser,
                Scanners.WHITESPACES.atLeast(1),
                toScanner(keywords.ago()),
                (amount, s1, unitInfo, s2, op) -> {
                    LocalDateTime now = LocalDateTime.now(clock);
                    return DateCalculator.calculateAgoDate(now, unitInfo.unit(), amount);
                }
        );

        Parser<DateValue> fromNowParser = Parsers.sequence(
                numberParser.optional(1),
                Scanners.WHITESPACES.many(),
                chronoUnitParser,
                Scanners.WHITESPACES.atLeast(1),
                toScanner(keywords.fromNow()),
                (amount, s1, unitInfo, s2, op) -> {
                    LocalDateTime now = LocalDateTime.now(clock);
                    return DateCalculator.calculateFromNowDate(now, unitInfo.unit(), amount);
                }
        );

        Parser<DateValue> inParser = Parsers.sequence(
                toScanner(keywords.in()),
                Scanners.WHITESPACES.atLeast(1),
                numberParser.optional(1),
                Scanners.WHITESPACES.many(),
                chronoUnitParser,
                (op, s1, amount, s2, unitInfo) -> {
                    LocalDateTime now = LocalDateTime.now(clock);
                    return DateCalculator.calculateFromNowDate(now, unitInfo.unit(), amount);
                }
        );

        return Parsers.or(dayOfWeekAgoParser, dayOfWeekFromNowParser, agoParser, fromNowParser, inParser);
    }

    private Parser<DateValue> createRelativePointWithTimeParser(LanguageKeywords keywords, Parser<DateValue> relativePoint, Parser<LocalTime> timeParser) {
        return Parsers.sequence(
                relativePoint,
                Scanners.WHITESPACES.atLeast(1),
                toScanner(keywords.at()).optional(),
                Scanners.WHITESPACES.many(),
                timeParser,
                (pointVal, s1, at, s2, time) -> switch (pointVal) {
                    case DateValue.AbsoluteDate ad -> {
                        LocalDateTime dt = LocalDateTime.of(ad.date().toLocalDate(), time);
                        yield new DateValue.AbsoluteDate(dt, false, true);
                    }
                    default -> throw new IllegalStateException("Unexpected DateValue type: " + pointVal.getClass());
                }
        );
    }

    private Parser<String> createRangeOp(LanguageKeywords keywords) {
        return Parsers.or(toScanner(keywords.rangeConnectorsInclusive()), toScanner(keywords.rangeConnectorsExclusive()));
    }

    private Parser<DateValue> createRangeParser(LanguageKeywords keywords, Parser<DateValue> finalAbsoluteDateTimeParser, Parser<String> rangeOp) {
        return Parsers.sequence(
                finalAbsoluteDateTimeParser,
                Scanners.WHITESPACES.many(),
                rangeOp,
                Scanners.WHITESPACES.many(),
                finalAbsoluteDateTimeParser,
                (fromValue, s1, op, s2, untilValue) -> {
                    LocalDateTime from = switch (fromValue) {
                        case DateValue.AbsoluteDate ad -> ad.date();
                        case DateValue.AbsoluteRange ar -> ar.from();
                    };

                    LocalDateTime until;
                    boolean isUntilInclusive;

                    switch (untilValue) {
                        case DateValue.AbsoluteDate ad -> {
                            until = ad.date();
                            isUntilInclusive = containsIgnoreCase(keywords.rangeConnectorsInclusive(), op);
                        }
                        case DateValue.AbsoluteRange ar -> {
                            if (containsIgnoreCase(keywords.rangeConnectorsInclusive(), op)) {
                                until = ar.until();
                                isUntilInclusive = ar.untilInclusive();
                            } else {
                                until = ar.from();
                                isUntilInclusive = false;
                            }
                        }
                    }

                    return new DateValue.AbsoluteRange(from, until, true, isUntilInclusive);
                }
        );
    }

    private Parser<DateValue> createStartOfParser(LanguageKeywords keywords, Parser<DateValue> base) {
        return Parsers.sequence(
                toScanner(keywords.startOf()),
                Scanners.WHITESPACES.atLeast(1),
                base,
                (op, spaces, dateValue) -> switch (dateValue) {
                    case DateValue.AbsoluteRange ar -> new DateValue.AbsoluteDate(ar.from(), true, ar.fromInclusive());
                    case DateValue.AbsoluteDate ad -> ad;
                }
        );
    }

    private Parser<DateValue> createEndOfParser(LanguageKeywords keywords, Parser<DateValue> base) {
        return Parsers.sequence(
                toScanner(keywords.endOf()),
                Scanners.WHITESPACES.atLeast(1),
                base,
                (op, spaces, dateValue) -> switch (dateValue) {
                    case DateValue.AbsoluteRange ar -> new DateValue.AbsoluteDate(ar.until(), true, false);
                    case DateValue.AbsoluteDate ad -> ad;
                    default -> throw new IllegalStateException("Unexpected DateValue type: " + dateValue.getClass());
                }
        );
    }

    private Parser<DateValue> createFirstDayOfParser(LanguageKeywords keywords, Parser<DateValue> base) {
        return Parsers.sequence(
                toScanner(keywords.firstDayOf()),
                Scanners.WHITESPACES.atLeast(1),
                base,
                (op, spaces, dateValue) -> switch (dateValue) {
                    case DateValue.AbsoluteRange ar -> new DateValue.AbsoluteRange(ar.from().with(LocalTime.MIN), ar.from().plusDays(1).with(LocalTime.MIN), true, false);
                    case DateValue.AbsoluteDate ad -> {
                        LocalDateTime dayStart = ad.date().with(LocalTime.MIN);
                        yield new DateValue.AbsoluteRange(dayStart, dayStart.plusDays(1), true, false);
                    }
                }
        );
    }

    private Parser<DateValue> createLastDayOfParser(LanguageKeywords keywords, Parser<DateValue> base) {
        return Parsers.sequence(
                toScanner(keywords.lastDayOf()),
                Scanners.WHITESPACES.atLeast(1),
                base,
                (op, spaces, dateValue) -> {
                    LocalDateTime until;
                    boolean inclusive;
                    switch (dateValue) {
                        case DateValue.AbsoluteRange ar -> {
                            until = ar.until();
                            inclusive = ar.untilInclusive();
                        }
                        case DateValue.AbsoluteDate ad -> {
                            until = ad.date().plusDays(1).with(LocalTime.MIN);
                            inclusive = false;
                        }
                        default -> throw new IllegalStateException("Unexpected DateValue type: " + dateValue.getClass());
                    }
                    LocalDateTime dayStart = until.minusDays(1).with(LocalTime.MIN);
                    return new DateValue.AbsoluteRange(dayStart, until, true, inclusive);
                }
        );
    }

    private Parser<DateValue> createBetweenParser(LanguageKeywords keywords, Parser<DateValue> finalAbsoluteDateTimeParser) {
        return Parsers.sequence(
                toScanner(keywords.between()),
                Scanners.WHITESPACES.atLeast(1),
                finalAbsoluteDateTimeParser,
                Scanners.WHITESPACES.atLeast(1),
                toScanner(keywords.and()),
                Scanners.WHITESPACES.atLeast(1),
                finalAbsoluteDateTimeParser,
                (op1, s1, fromValue, s2, op2, s3, untilValue) -> {
                    LocalDateTime from = switch (fromValue) {
                        case DateValue.AbsoluteDate ad -> ad.date();
                        case DateValue.AbsoluteRange ar -> ar.from();
                    };

                    LocalDateTime until;
                    boolean isUntilInclusive;

                    switch (untilValue) {
                        case DateValue.AbsoluteDate ad -> {
                            until = ad.date();
                            isUntilInclusive = true; // "between A and B" is usually inclusive of the day B
                        }
                        case DateValue.AbsoluteRange ar -> {
                            until = ar.until();
                            isUntilInclusive = ar.untilInclusive();
                        }
                    }

                    return new DateValue.AbsoluteRange(from, until, true, isUntilInclusive);
                }
        );
    }

    private Parser<DateValue> createUntilParser(LanguageKeywords keywords, Parser<DateValue> finalAbsoluteDateTimeParser) {
        Parser<String> untilOp = Parsers.or(toScanner(keywords.untilInclusive()), toScanner(keywords.untilExclusive()));
        return Parsers.sequence(
                untilOp,
                Scanners.WHITESPACES.many(),
                finalAbsoluteDateTimeParser,
                (op, spaces, dateValue) -> {
                    LocalDateTime date;
                    boolean inclusive;
                    switch (dateValue) {
                        case DateValue.AbsoluteDate ad -> {
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
                        }
                        case DateValue.AbsoluteRange ar -> {
                            if (containsIgnoreCase(keywords.untilInclusive(), op)) {
                                date = ar.until();
                                inclusive = ar.untilInclusive();
                            } else {
                                date = ar.from();
                                inclusive = false;
                            }
                        }
                    }
                    return new DateValue.AbsoluteRange(LocalDateTime.MIN, date, false, inclusive);
                }
        );
    }

    private Parser<DateValue> createFromParser(LanguageKeywords keywords, Parser<DateValue> dateOnlyParser, Parser<DateValue> finalAbsoluteDateTimeParser) {
        Parser<String> fromOp = Parsers.or(toScanner(keywords.fromInclusive()), toScanner(keywords.fromExclusive()));
        Parser<DateValue> exclusive = Parsers.sequence(
                toScanner(keywords.fromExclusive()),
                Scanners.WHITESPACES.many(),
                dateOnlyParser,
                (op, spaces, dateValue) -> {
                    LocalDateTime date = switch (dateValue) {
                        case DateValue.AbsoluteRange ar -> ar.until();
                        default -> throw new IllegalStateException("Unexpected DateValue type for dateOnlyParser: " + dateValue.getClass());
                    };
                    return new DateValue.AbsoluteRange(date, LocalDateTime.MAX, true, false);
                }
        );

        Parser<DateValue> general = Parsers.sequence(
                fromOp,
                Scanners.WHITESPACES.many(),
                finalAbsoluteDateTimeParser,
                (op, spaces, dateValue) -> {
                    LocalDateTime date;
                    boolean inclusive = containsIgnoreCase(keywords.fromInclusive(), op);
                    switch (dateValue) {
                        case DateValue.AbsoluteDate ad -> {
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
                        }
                        case DateValue.AbsoluteRange ar -> {
                            if (inclusive) {
                                date = ar.from();
                            } else {
                                date = ar.until();
                                inclusive = true;
                            }
                        }
                    }
                    return new DateValue.AbsoluteRange(date, LocalDateTime.MAX, inclusive, false);
                }
        );

        return Parsers.or(exclusive, general);
    }

    private static boolean containsIgnoreCase(Set<String> set, String value) {
        return set.stream().anyMatch(s -> s.equalsIgnoreCase(value));
    }

    private static Parser<String> toScanner(Set<String> keywords) {
        return Parsers.or(keywords.stream().sorted(BY_LENGTH_DESC).map(Scanners::stringCaseInsensitive).toList()).source();
    }

    private static Parser<DateValue> createAbsoluteDateTimeParser(LanguageKeywords keywords, Clock clock, Parser<LocalTime> timeParser) {
        Parser<DateValue> nowParser = toScanner(keywords.now()).map(ignored -> new DateValue.AbsoluteDate(LocalDateTime.now(clock), false, true));

        Parser<DateValue> relativeDate = createRelativeDateParser(keywords, clock);

        Parser<DateValue> relativeDateWithTime = Parsers.sequence(
                relativeDate,
                Scanners.WHITESPACES.atLeast(1),
                toScanner(keywords.at()).optional(),
                Scanners.WHITESPACES.many(),
                timeParser,
                (dateVal, s1, at, s2, time) -> {
                    LocalDateTime dt = switch (dateVal) {
                        case DateValue.AbsoluteRange ar -> LocalDateTime.of(ar.from().toLocalDate(), time);
                        case DateValue.AbsoluteDate ad -> LocalDateTime.of(ad.date().toLocalDate(), time);
                    };
                    return new DateValue.AbsoluteDate(dt, false, true);
                }
        );

        Parser<DateValue> timeOnly = timeParser.map(time -> {
            LocalDate today = LocalDate.now(clock);
            return new DateValue.AbsoluteDate(LocalDateTime.of(today, time), false, true);
        });

        Parser<LocalDateTime> dateTimeParser = Parsers.sequence(
                DATE,
                Parsers.or(Scanners.WHITESPACES.atLeast(1), Scanners.stringCaseInsensitive("T")),
                timeParser,
                (date, separator, time) -> LocalDateTime.of(date, time)
        );

        return Parsers.longest(
                nowParser,
                dateTimeParser.map(dt -> new DateValue.AbsoluteDate(dt, false, true)),
                relativeDateWithTime,
                createDateOnlyParser(keywords, clock),
                timeOnly
        );
    }

    private static Parser<DateValue> createRelativeDateParser(LanguageKeywords keywords, Clock clock) {
        return Parsers.or(
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
                        // Maps days of week to previous or same day range
                        keywords.daysOfWeek().entrySet().stream().map(entry -> Scanners.stringCaseInsensitive(entry.getKey()).map(ignored -> {
                            LocalDate day = LocalDate.now(clock).with(TemporalAdjusters.previousOrSame(entry.getValue()));
                            return (DateValue) new DateValue.AbsoluteRange(day.atStartOfDay(), day.plusDays(1).atStartOfDay(), true, false);
                        })),
                        // Maps "next <day>" to next occurrence of day range
                        keywords.daysOfWeek().entrySet().stream().map(entry -> Parsers.sequence(
                                toScanner(keywords.next()),
                                Scanners.WHITESPACES.atLeast(1),
                                Scanners.stringCaseInsensitive(entry.getKey()),
                                (op, s, dayName) -> {
                                    LocalDate day = LocalDate.now(clock).with(TemporalAdjusters.next(entry.getValue()));
                                    return (DateValue) new DateValue.AbsoluteRange(day.atStartOfDay(), day.plusDays(1).atStartOfDay(), true, false);
                                }
                        )),
                        // Maps "last <day>" to previous occurrence of day range
                        keywords.daysOfWeek().entrySet().stream().map(entry -> Parsers.sequence(
                                toScanner(keywords.last()),
                                Scanners.WHITESPACES.atLeast(1),
                                Scanners.stringCaseInsensitive(entry.getKey()),
                                (op, s, dayName) -> {
                                    LocalDate day = LocalDate.now(clock).with(TemporalAdjusters.previous(entry.getValue()));
                                    return (DateValue) new DateValue.AbsoluteRange(day.atStartOfDay(), day.plusDays(1).atStartOfDay(), true, false);
                                }
                        ))
                ).flatMap(s -> s).toList()
        );
    }

    private static Parser<DateValue> createDateOnlyParser(LanguageKeywords keywords, Clock clock) {
        return Parsers.or(
                DATE_ONLY.notFollowedBy(Scanners.WHITESPACES.many().next(Scanners.isChar(Character::isDigit)))
                        .map(date -> new DateValue.AbsoluteRange(date, date.plusDays(1), true, false)),
                ISO_WEEK,
                YEAR_MONTH,
                createRelativeDateParser(keywords, clock)
        );
    }

    private static Parser<LocalTime> createTimeParser(LanguageKeywords keywords) {
        Parser<LocalTime> time24h = Parsers.or(
                Patterns.regex("\\d{2}:\\d{2}:\\d{2}")
                        .toScanner("time-with-seconds")
                        .source()
                        .map(LocalTime::parse),
                Patterns.regex("\\d{2}:\\d{2}")
                        .toScanner("time-without-seconds")
                        .source()
                        .map(s -> LocalTime.parse(s + ":00"))
        );

        Parser<Integer> hourParser = Patterns.INTEGER.toScanner("hour").source().map(Integer::parseInt);
        Parser<Integer> minuteParser = Patterns.regex("\\d{2}").toScanner("minute").source().map(Integer::parseInt);
        Parser<Integer> secondParser = Patterns.regex("\\d{2}").toScanner("second").source().map(Integer::parseInt);

        Parser<Boolean> amParser = toScanner(keywords.am()).map(ignored -> true);
        Parser<Boolean> pmParser = toScanner(keywords.pm()).map(ignored -> false);
        Parser<Boolean> amPmParser = Parsers.or(amParser, pmParser);

        Parser<LocalTime> time12h = Parsers.sequence(
                hourParser,
                Parsers.sequence(Scanners.isChar(':'), minuteParser).optional(),
                Parsers.sequence(Scanners.isChar(':'), secondParser).optional(),
                Scanners.WHITESPACES.many(),
                amPmParser,
                (hour, min, sec, s, isAm) -> {
                    int h = hour;
                    if (!isAm && h < 12) h += 12;
                    if (isAm && h == 12) h = 0;
                    int m = min == null ? 0 : min;
                    int s1 = sec == null ? 0 : sec;
                    return LocalTime.of(h, m, s1);
                }
        );

        return Parsers.longest(time24h, time12h);
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
    
    private static final Parser<DateValue.AbsoluteRange> ISO_WEEK = Patterns.regex("\\d{4}-W\\d{1,2}")
            .toScanner("iso-week")
            .source()
            .map(s -> {
                String[] parts = s.split("-W");
                int year = Integer.parseInt(parts[0]);
                int week = Integer.parseInt(parts[1]);
                LocalDate start = LocalDate.of(year, 1, 4) // ISO-8601 week 1 is the week with Jan 4th
                        .with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                        .plusWeeks(week - 1L);
                return new DateValue.AbsoluteRange(start.atStartOfDay(), start.plusWeeks(1).atStartOfDay(), true, false);
            });

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
        return dateValueParser.followedBy(Parsers.EOF);
    }

    public Parser<DateValue> componentParser() {
        return dateValueParser;
    }

    public static final Parser<DateValue> DATE_VALUE_PARSER = new DateValueParser().parser();

}
