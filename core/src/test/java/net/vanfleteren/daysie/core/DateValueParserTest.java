package net.vanfleteren.daysie.core;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class DateValueParserTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-02-14T10:00:00Z"), ZoneId.of("UTC"));

    @Nested
    class SingleParserTests {
        @ParameterizedTest(name = "parse \"{0}\" returns {1}")
        @CsvSource({
                "2026-02-03, 2026-02-03T00:00:00",
                "2026-02-03 12:34:56, 2026-02-03T12:34:56",
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

                "'2026-01-01 to 2026-02-01', '[2026-01-01T00:00, 2026-02-01T00:00]'",
                "'2026-01-01 - 2026-02-01', '[2026-01-01T00:00, 2026-02-01T00:00]'",
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
                "today,     '[2026-02-14T00:00,2026-02-14T00:00]'",
                "yesterday, '[2026-02-13T00:00,2026-02-13T00:00]'",
                "tomorrow,  '[2026-02-15T00:00,2026-02-15T00:00]'",
                "vandaag,   '[2026-02-14T00:00,2026-02-14T00:00]'",
                "gisteren,  '[2026-02-13T00:00,2026-02-13T00:00]'",
                "morgen,    '[2026-02-15T00:00,2026-02-15T00:00]'"
        })
        void parse_relativeInput_returnsExpectedToString(String input, String expectedToString) {
            LanguageKeywords combined = LanguageKeywords.combine(java.util.List.of(LanguageKeywords.ENGLISH, LanguageKeywords.DUTCH));
            DateValueParser parser = new DateValueParser(combined, FIXED_CLOCK);
            DateValue result = parser.parser().parse(input);
            assertThat(result.toString()).isEqualTo(expectedToString);
        }

        @ParameterizedTest(name = "parse \"{0}\" with operator returns {1}")
        @CsvSource({
                "before yesterday, '(-∞,2026-02-13T00:00)'",
                "after today,      '(2026-02-14T00:00, ∞)'",
                "since yesterday,  '[2026-02-13T00:00, ∞)'",
                "until tomorrow,   '(-∞,2026-02-15T00:00]'",
                "gisteren tot vandaag, '[2026-02-13T00:00, 2026-02-14T00:00]'"
        })
        void parse_relativeInputWithOperator_returnsExpectedToString(String input, String expectedToString) {
            LanguageKeywords combined = LanguageKeywords.combine(java.util.List.of(LanguageKeywords.ENGLISH, LanguageKeywords.DUTCH));
            DateValueParser parser = new DateValueParser(combined, FIXED_CLOCK);
            DateValue result = parser.parser().parse(input);
            assertThat(result.toString()).isEqualTo(expectedToString);
        }
    }

    @Nested
    class MultiLanguageParserTests {
        @ParameterizedTest(name = "Dutch parse \"{0}\" returns {1}")
        @CsvSource({
                "'na 2026-01-01',       '(2026-01-01T00:00, ∞)'",
                "'sinds 2026-01-01',    '[2026-01-01T00:00, ∞)'",
                "'voor 2026-01-01',     '(-∞,2026-01-01T00:00)'",
                "'tot 2026-01-01',      '(-∞,2026-01-01T00:00)'",
                "'tot en met 2026-01-01', '(-∞,2026-01-01T00:00]'",
                "'2026-01-01 tot 2026-02-01', '[2026-01-01T00:00, 2026-02-01T00:00]'",
                "'2026-01-01 t/m 2026-02-01', '[2026-01-01T00:00, 2026-02-01T00:00]'",
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
            LanguageKeywords combined = LanguageKeywords.combine(java.util.List.of(LanguageKeywords.ENGLISH, LanguageKeywords.DUTCH));
            DateValueParser combinedParser = new DateValueParser(combined);
            DateValue result = combinedParser.parser().parse(input);
            assertThat(result.toString()).isEqualTo(expectedToString);
        }
    }
}
