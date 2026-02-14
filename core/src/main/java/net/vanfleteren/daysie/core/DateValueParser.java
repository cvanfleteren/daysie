package net.vanfleteren.daysie.core;

import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.jparsec.Scanners;
import org.jparsec.pattern.Patterns;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.stream.Stream;

public class DateValueParser {

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
        Parser<String> untilInclusive = Parsers.or(keywords.untilInclusive().stream().sorted((a, b) -> b.length() - a.length()).map(Scanners::string).toList()).source();
        Parser<String> untilExclusive = Parsers.or(keywords.untilExclusive().stream().sorted((a, b) -> b.length() - a.length()).map(Scanners::string).toList()).source();
        Parser<String> untilOp = Parsers.or(untilInclusive, untilExclusive);

        Parser<DateValue> untilAbsoluteDate = Parsers.sequence(
                untilOp,
                Scanners.WHITESPACES.many(),
                absoluteDateTimeParser,
                (op, spaces, date) -> new DateValue.UntilAbsoluteDate(date, keywords.untilInclusive().contains(op))
        );

        Parser<String> fromInclusive = Parsers.or(keywords.fromInclusive().stream().sorted((a, b) -> b.length() - a.length()).map(Scanners::string).toList()).source();
        Parser<String> fromExclusive = Parsers.or(keywords.fromExclusive().stream().sorted((a, b) -> b.length() - a.length()).map(Scanners::string).toList()).source();
        Parser<String> fromOp = Parsers.or(fromInclusive, fromExclusive);

        Parser<DateValue> fromAbsoluteDate = Parsers.sequence(
                fromOp,
                Scanners.WHITESPACES.many(),
                absoluteDateTimeParser,
                (op, spaces, date) -> new DateValue.FromAbsoluteDate(date, keywords.fromInclusive().contains(op))
        );

        Parser<String> rangeInclusive = Parsers.or(keywords.rangeConnectorsInclusive().stream().sorted((a, b) -> b.length() - a.length()).map(Scanners::string).toList()).source();
        Parser<String> rangeExclusive = Parsers.or(keywords.rangeConnectorsExclusive().stream().sorted((a, b) -> b.length() - a.length()).map(Scanners::string).toList()).source();
        Parser<String> rangeOp = Parsers.or(rangeInclusive, rangeExclusive);

        Parser<DateValue> absoluteRange = Parsers.sequence(
                absoluteDateTimeParser,
                Scanners.WHITESPACES.many(),
                rangeOp,
                Scanners.WHITESPACES.many(),
                absoluteDateTimeParser,
                (from, s1, op, s2, until) -> new DateValue.AbsoluteRange(from, until, true, keywords.rangeConnectorsInclusive().contains(op))
        );

        this.dateValueParser = Parsers.or(
                absoluteRange,
                untilAbsoluteDate,
                fromAbsoluteDate,
                absoluteDateTimeParser.map(DateValue.AbsoluteDate::new)
        );
    }

    private static Parser<LocalDateTime> createAbsoluteDateTimeParser(LanguageKeywords keywords, Clock clock) {
        Parser<LocalDateTime> relativeDate = Parsers.or(
                Stream.of(
                        keywords.today().stream().map(s -> Scanners.string(s).map(ignored -> LocalDate.now(clock).atStartOfDay())),
                        keywords.yesterday().stream().map(s -> Scanners.string(s).map(ignored -> LocalDate.now(clock).minusDays(1).atStartOfDay())),
                        keywords.tomorrow().stream().map(s -> Scanners.string(s).map(ignored -> LocalDate.now(clock).plusDays(1).atStartOfDay()))
                ).flatMap(s -> s).sorted((a, b) -> 0).toList() // Placeholder to allow sorting if needed, but we use longest() below
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
            Scanners.WHITESPACES.atLeast(1),
            TIME,
            (date, space, time) -> LocalDateTime.of(date, time)
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
