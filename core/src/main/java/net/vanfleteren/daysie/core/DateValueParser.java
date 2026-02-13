package net.vanfleteren.daysie.core;

import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.jparsec.Scanners;
import org.jparsec.pattern.Patterns;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class DateValueParser {

    private final Parser<DateValue> dateValueParser;

    public DateValueParser() {
        this(LanguageKeywords.ENGLISH);
    }

    public DateValueParser(LanguageKeywords keywords) {
        Parser<String> untilInclusive = Parsers.or(keywords.untilInclusive().stream().map(Scanners::string).toList()).source();
        Parser<String> untilExclusive = Parsers.or(keywords.untilExclusive().stream().map(Scanners::string).toList()).source();
        Parser<String> untilOp = Parsers.or(untilInclusive, untilExclusive);

        Parser<DateValue> untilAbsoluteDate = Parsers.sequence(
                untilOp,
                Scanners.WHITESPACES.many(),
                ABSOLUTE_DATE_TIME,
                (op, spaces, date) -> new DateValue.UntilAbsoluteDate(date, keywords.untilInclusive().contains(op))
        );

        Parser<String> fromInclusive = Parsers.or(keywords.fromInclusive().stream().map(Scanners::string).toList()).source();
        Parser<String> fromExclusive = Parsers.or(keywords.fromExclusive().stream().map(Scanners::string).toList()).source();
        Parser<String> fromOp = Parsers.or(fromInclusive, fromExclusive);

        Parser<DateValue> fromAbsoluteDate = Parsers.sequence(
                fromOp,
                Scanners.WHITESPACES.many(),
                ABSOLUTE_DATE_TIME,
                (op, spaces, date) -> new DateValue.FromAbsoluteDate(date, keywords.fromInclusive().contains(op))
        );

        this.dateValueParser = Parsers.or(
                untilAbsoluteDate,
                fromAbsoluteDate,
                ABSOLUTE_DATE_TIME.map(DateValue.AbsoluteDate::new)
        );
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

    static final Parser<LocalDateTime> ABSOLUTE_DATE_TIME = Parsers.or(DATE_TIME, DATE_ONLY);

    public Parser<DateValue> parser() {
        return dateValueParser;
    }

    public static final Parser<DateValue> DATE_VALUE_PARSER = new DateValueParser().parser();

}
