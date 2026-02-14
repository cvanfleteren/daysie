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

                "'> 2026-02-01',           '(2026-02-01T00:00, ∞)'",
                "'after 2026-01-01',       '(2026-01-01T00:00, ∞)'",
                "'since 2026-01-01',       '[2026-01-01T00:00, ∞)'",
                "'>= 2026-02-01',          '[2026-02-01T00:00, ∞)'",
                "'>= 2026-02-03 12:34:56', '[2026-02-03T12:34:56, ∞)'",

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
        @ParameterizedTest(name = "parse \"{0}\" with fixed clock returns {1}")
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

                "last week,              '[2026-02-02T00:00,2026-02-08T00:00]'",
                "vorige week,            '[2026-02-02T00:00,2026-02-08T00:00]'",
                "last month,             '[2026-01-01T00:00,2026-01-31T00:00]'",
                "vorige maand,           '[2026-01-01T00:00,2026-01-31T00:00]'",
                "last year,              '[2025-01-01T00:00,2025-12-31T00:00]'",
                "last quarter,           '[2025-10-01T00:00,2025-12-31T00:00]'",
                "last 2 months,          '[2025-12-01T00:00,2026-01-31T00:00]'",
                "previous 2 months,      '[2025-12-01T00:00,2026-01-31T00:00]'",
                "last 3 days,            '[2026-02-11T00:00,2026-02-13T00:00]'",
                "vorige 2 kwartalen,     '[2025-07-01T00:00,2025-12-31T00:00]'",
                "TODAY,                  '[2026-02-14T00:00,2026-02-14T00:00]'",
                "Yesterday,              '[2026-02-13T00:00,2026-02-13T00:00]'",
                "MONDAY,                 '[2026-02-09T00:00,2026-02-09T00:00]'",
                "Vorige Week,            '[2026-02-02T00:00,2026-02-08T00:00]'",
        })
        void parse_relativeInput_returnsExpectedToString(String input, String expectedToString) {
            LanguageKeywords combined = LanguageKeywords.combine(List.of(LanguageKeywords.ENGLISH, LanguageKeywords.DUTCH));
            DateValueParser parser = new DateValueParser(combined, FIXED_CLOCK);
            DateValue result = parser.parser().parse(input);
            assertThat(result.toString()).isEqualTo(expectedToString);
        }

        @ParameterizedTest(name = "parse \"{0}\" with operator returns {1}")
        @CsvSource({
                "before yesterday,       '(-∞,2026-02-13T00:00)'",
                "after today,            '(2026-02-14T00:00, ∞)'",
                "since yesterday,        '[2026-02-13T00:00, ∞)'",
                "until tomorrow,         '(-∞,2026-02-15T00:00]'",
                "since saturday,         '[2026-02-14T00:00, ∞)'",
                "after friday,           '(2026-02-13T00:00, ∞)'",
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
                "'na 2026-01-01',               '(2026-01-01T00:00, ∞)'",
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
                "'after 2026-01-01',    '(2026-01-01T00:00, ∞)'",
                "'na 2026-01-01',       '(2026-01-01T00:00, ∞)'",
                "'since 2026-01-01',    '[2026-01-01T00:00, ∞)'",
                "'sinds 2026-01-01',    '[2026-01-01T00:00, ∞)'",
        })
        void parse_combinedInput_returnsExpectedToString(String input, String expectedToString) {
            LanguageKeywords combined = LanguageKeywords.combine(List.of(LanguageKeywords.ENGLISH, LanguageKeywords.DUTCH));
            DateValueParser combinedParser = new DateValueParser(combined);
            DateValue result = combinedParser.parser().parse(input);
            assertThat(result.toString()).isEqualTo(expectedToString);
        }
    }
}
