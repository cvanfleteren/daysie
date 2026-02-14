package net.vanfleteren.daysie.core;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DateValueParserTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-02-14T10:00:00Z"), ZoneId.of("UTC"));

    @Nested
    class SingleParserTests {
        @ParameterizedTest(name = "parse \"{0}\" returns {1}")
        @CsvSource({
                "2026-02-03, 2026-02-03T00:00:00",
                "2026-02-03 12:34:56, 2026-02-03T12:34:56",
                "2026-02-03T12:34:56, 2026-02-03T12:34:56",
                "2026-02-03t12:34:56, 2026-02-03T12:34:56",
                "2026-02-03   12:34:56, 2026-02-03T12:34:56"
        })
        void parse_input_returnsDateTime(String input, String expected) {
            LocalDateTime result = new DateValueParser().absoluteDateTimeParser().parse(input);
            assertThat(result).isEqualTo(LocalDateTime.parse(expected));
        }
    }

    @Nested
    class DateValueParserTests {
        @ParameterizedTest(name = "parse \"{0}\" returns {1}")
        @CsvSource({
                "'2026-02-03',              '[2026-02-03T00:00,2026-02-03T00:00]'",
                "'2026-02-03 12:34:56',     '[2026-02-03T12:34:56,2026-02-03T12:34:56]'",

                "'< 2026-02-01',           '(-∞,2026-02-01T00:00)'",
                "'before 2026-01-01',      '(-∞,2026-01-01T00:00)'",
                "'until 2026-01-01',       '(-∞,2026-01-01T00:00]'",
                "'<= 2026-02-01',          '(-∞,2026-02-01T00:00]'",
                "'<= 2026-02-03 12:34:56', '(-∞,2026-02-03T12:34:56]'",

                "'> 2026-02-01',           '[2026-02-02T00:00, ∞)'",
                "'after 2026-01-01',       '[2026-01-02T00:00, ∞)'",
                "'since 2026-01-01',       '[2026-01-01T00:00, ∞)'",
                "'>= 2026-02-01',          '[2026-02-01T00:00, ∞)'",
                "'>= 2026-02-03 12:34:56', '[2026-02-03T12:34:56, ∞)'",
                "'after 2026-01-01 12:00:00', '(2026-01-01T12:00, ∞)'",

                "'BEFORE 2026-01-01',      '(-∞,2026-01-01T00:00)'",
                "'SiNcE 2026-01-01',       '[2026-01-01T00:00, ∞)'",

                "'2026-01-01 to 2026-02-01', '[2026-01-01T00:00,2026-02-01T00:00]'",
                "'2026-01-01 - 2026-02-01',  '[2026-01-01T00:00,2026-02-01T00:00]'",
        })
        void parse_input_returnsExpectedToString(String input, String expectedToString) {
            DateValue result = DateValueParser.DATE_VALUE_PARSER.parse(input);
            assertThat(result.toString()).isEqualTo(expectedToString);
        }
    }

    @Nested
    class RelativeDateTests {
        @ParameterizedTest(name = "parse fixed relative \"{0}\" returns {1}")
        @CsvSource({
                "today,                  '[2026-02-14T00:00,2026-02-14T00:00]'",
                "yesterday,              '[2026-02-13T00:00,2026-02-13T00:00]'",
                "tomorrow,               '[2026-02-15T00:00,2026-02-15T00:00]'",
                "vandaag,                '[2026-02-14T00:00,2026-02-14T00:00]'",
                "gisteren,               '[2026-02-13T00:00,2026-02-13T00:00]'",
                "morgen,                 '[2026-02-15T00:00,2026-02-15T00:00]'",
                "monday,                 '[2026-02-09T00:00,2026-02-09T00:00]'",
                "saturday,               '[2026-02-14T00:00,2026-02-14T00:00]'",
                "zaterdag,               '[2026-02-14T00:00,2026-02-14T00:00]'",
                "sunday,                 '[2026-02-08T00:00,2026-02-08T00:00]'",
                "TODAY,                  '[2026-02-14T00:00,2026-02-14T00:00]'",
                "Yesterday,              '[2026-02-13T00:00,2026-02-13T00:00]'",
                "MONDAY,                 '[2026-02-09T00:00,2026-02-09T00:00]'",
        })
        void parse_fixedRelativeInput_returnsExpectedToString(String input, String expectedToString) {
            parseAndAssert(input, expectedToString);
        }

        @ParameterizedTest(name = "parse past relative \"{0}\" returns {1}")
        @CsvSource({
                "last week,              '[2026-02-02T00:00,2026-02-09T00:00)'",
                "vorige week,            '[2026-02-02T00:00,2026-02-09T00:00)'",
                "Vorige Week,            '[2026-02-02T00:00,2026-02-09T00:00)'",
                "afgelopen week,         '[2026-02-02T00:00,2026-02-09T00:00)'",
                "last month,             '[2026-01-01T00:00,2026-02-01T00:00)'",
                "last 1 month,           '[2026-01-01T00:00,2026-02-01T00:00)'",
                "vorige maand,           '[2026-01-01T00:00,2026-02-01T00:00)'",
                "last year,              '[2025-01-01T00:00,2026-01-01T00:00)'",
                "last quarter,           '[2025-10-01T00:00,2026-01-01T00:00)'",
                "last 2 months,          '[2025-12-01T00:00,2026-02-01T00:00)'",
                "previous 2 months,      '[2025-12-01T00:00,2026-02-01T00:00)'",
                "vorige 2 kwartalen,     '[2025-07-01T00:00,2026-01-01T00:00)'",
                "last hour,              '[2026-02-14T09:00,2026-02-14T10:00)'",
                "last 2 hours,           '[2026-02-14T08:00,2026-02-14T10:00)'",
                "afgelopen 1 uur,        '[2026-02-14T09:00,2026-02-14T10:00)'",
                "past day,               '[2026-02-13T00:00,2026-02-14T10:00)'",
                "last 3 days,            '[2026-02-11T00:00,2026-02-14T10:00)'",
                "past 3 days,            '[2026-02-11T00:00,2026-02-14T10:00)'",
                "last 5 minutes,         '[2026-02-14T09:55,2026-02-14T10:00)'",
                "afgelopen 10 minuten,   '[2026-02-14T09:50,2026-02-14T10:00)'",
        })
        void parse_pastRelativeInput_returnsExpectedToString(String input, String expectedToString) {
            parseAndAssert(input, expectedToString);
        }

        @ParameterizedTest(name = "parse future relative \"{0}\" returns {1}")
        @CsvSource({
                "next hour,              '[2026-02-14T10:00,2026-02-14T11:00)'",
                "next 2 hours,           '[2026-02-14T10:00,2026-02-14T12:00)'",
                "next 20 hours,          '[2026-02-14T10:00,2026-02-15T06:00)'",
                "next 2 days,            '[2026-02-14T10:00,2026-02-17T00:00)'",
                "next 15 minutes,        '[2026-02-14T10:00,2026-02-14T10:15)'",
                "volgende 5 minutes,     '[2026-02-14T10:00,2026-02-14T10:05)'",
                "volgende week,          '[2026-02-16T00:00,2026-02-23T00:00)'",
                "next month,             '[2026-03-01T00:00,2026-04-01T00:00)'",
                "next year,              '[2027-01-01T00:00,2028-01-01T00:00)'",
                "volgende 2 kwartalen,   '[2026-04-01T00:00,2026-10-01T00:00)'",
        })
        void parse_futureRelativeInput_returnsExpectedToString(String input, String expectedToString) {
            parseAndAssert(input, expectedToString);
        }

        @ParameterizedTest(name = "parse current relative \"{0}\" returns {1}")
        @CsvSource({
                "this hour,               '[2026-02-14T10:00,2026-02-14T11:00)'",
                "this day,                '[2026-02-14T00:00,2026-02-15T00:00)'",
                "this week,               '[2026-02-09T00:00,2026-02-16T00:00)'",
                "this month,              '[2026-02-01T00:00,2026-03-01T00:00)'",
                "deze week,               '[2026-02-09T00:00,2026-02-16T00:00)'",
                "dit jaar,                '[2026-01-01T00:00,2027-01-01T00:00)'",
                "this 2 months,           '[2026-01-01T00:00,2026-03-01T00:00)'",
        })
        void parse_thisRelativeInput_returnsExpectedToString(String input, String expectedToString) {
            parseAndAssert(input, expectedToString);
        }

        private void parseAndAssert(String input, String expectedToString) {
            LanguageKeywords combined = LanguageKeywords.combine(List.of(LanguageKeywords.ENGLISH, LanguageKeywords.DUTCH));
            DateValueParser parser = new DateValueParser(combined, FIXED_CLOCK);
            DateValue result = parser.parser().parse(input);
            assertThat(result.toString()).isEqualTo(expectedToString);
        }

        @ParameterizedTest(name = "parse \"{0}\" with operator returns {1}")
        @CsvSource({
                "before yesterday,       '(-∞,2026-02-13T00:00)'",
                "after today,            '[2026-02-15T00:00, ∞)'",
                "since yesterday,        '[2026-02-13T00:00, ∞)'",
                "until tomorrow,         '(-∞,2026-02-15T00:00]'",
                "since saturday,         '[2026-02-14T00:00, ∞)'",
                "after friday,           '[2026-02-14T00:00, ∞)'",
                "gisteren ToT vandaag,   '[2026-02-13T00:00,2026-02-14T00:00]'",
                "sunday to yesterday,    '[2026-02-08T00:00,2026-02-13T00:00]'"
        })
        void parse_relativeInputWithOperator_returnsExpectedToString(String input, String expectedToString) {
            LanguageKeywords combined = LanguageKeywords.combine(List.of(LanguageKeywords.ENGLISH, LanguageKeywords.DUTCH));
            DateValueParser parser = new DateValueParser(combined, FIXED_CLOCK);
            DateValue result = parser.parser().parse(input);
            assertThat(result.toString()).isEqualTo(expectedToString);
        }
    }

    @Nested
    class MultiLanguageParserTests {
        @ParameterizedTest(name = "Dutch parse \"{0}\" returns {1}")
        @CsvSource({
                "'na 2026-01-01',               '[2026-01-02T00:00, ∞)'",
                "'sinds 2026-01-01',            '[2026-01-01T00:00, ∞)'",
                "'voor 2026-01-01',             '(-∞,2026-01-01T00:00)'",
                "'tot 2026-01-01',              '(-∞,2026-01-01T00:00)'",
                "'tot en met 2026-01-01',       '(-∞,2026-01-01T00:00]'",
                "'2026-01-01 tot 2026-02-01',   '[2026-01-01T00:00,2026-02-01T00:00]'",
                "'2026-01-01 t/m 2026-02-01',   '[2026-01-01T00:00,2026-02-01T00:00]'",
        })
        void parse_dutchInput_returnsExpectedToString(String input, String expectedToString) {
            DateValueParser dutchParser = new DateValueParser(LanguageKeywords.DUTCH);
            DateValue result = dutchParser.parser().parse(input);
            assertThat(result.toString()).isEqualTo(expectedToString);
        }

        @ParameterizedTest(name = "Combined parse \"{0}\" returns {1}")
        @CsvSource({
                "'after 2026-01-01',       '[2026-01-02T00:00, ∞)'",
                "'na 2026-01-01',          '[2026-01-02T00:00, ∞)'",
                "'na 2026-01-01 10:00:00', '(2026-01-01T10:00, ∞)'",
                "'since 2026-01-01',       '[2026-01-01T00:00, ∞)'",
                "'sinds 2026-01-01',       '[2026-01-01T00:00, ∞)'",
        })
        void parse_combinedInput_returnsExpectedToString(String input, String expectedToString) {
            LanguageKeywords combined = LanguageKeywords.combine(List.of(LanguageKeywords.ENGLISH, LanguageKeywords.DUTCH));
            DateValueParser combinedParser = new DateValueParser(combined);
            DateValue result = combinedParser.parser().parse(input);
            assertThat(result.toString()).isEqualTo(expectedToString);
        }
    }
}
