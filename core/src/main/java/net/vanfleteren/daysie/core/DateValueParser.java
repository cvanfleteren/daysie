package net.vanfleteren.daysie.core;

import org.jparsec.Parser;
import org.jparsec.Parsers;
import org.jparsec.Scanners;
import org.jparsec.pattern.Patterns;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class DateValueParser {

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

    private static final Parser<DateValue> UNTIL_ABSOLUTE_DATE = Parsers.sequence(
            Parsers.or(
                    Scanners.string("<="),
                    Scanners.string("<"),
                    Scanners.string("before"),
                    Scanners.string("until")
            ).source(),
            Scanners.WHITESPACES.many(),
            ABSOLUTE_DATE_TIME,
            (op, spaces, date) -> new DateValue.UntilAbsoluteDate(date, op.equals("<=") || op.equals("until"))
    );

    private static final Parser<DateValue> FROM_ABSOLUTE_DATE = Parsers.sequence(
            Parsers.or(
                    Scanners.string(">="),
                    Scanners.string(">"),
                    Scanners.string("after"),
                    Scanners.string("since")
            ).source(),
            Scanners.WHITESPACES.many(),
            ABSOLUTE_DATE_TIME,
            (op, spaces, date) -> new DateValue.FromAbsoluteDate(date, op.equals(">=") || op.equals("since"))
    );

    public static final Parser<DateValue> DATE_VALUE_PARSER = Parsers.or(
            UNTIL_ABSOLUTE_DATE,
            FROM_ABSOLUTE_DATE,
            ABSOLUTE_DATE_TIME.map(DateValue.AbsoluteDate::new)
    );

}
