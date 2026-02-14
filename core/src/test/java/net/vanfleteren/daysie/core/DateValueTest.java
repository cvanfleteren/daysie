package net.vanfleteren.daysie.core;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;

import static net.vanfleteren.daysie.core.DateValue.*;
import static org.assertj.core.api.Assertions.assertThat;

class DateValueTest {

    private static final LocalDateTime DATE_1 = LocalDateTime.of(2026, 2, 13, 22, 34);
    private static final LocalDateTime DATE_2 = LocalDateTime.of(2026, 2, 14, 22, 34);

    @Nested
    class AbsoluteRangeTest {
        @ParameterizedTest(name = "toString with fromInclusive={0}, untilInclusive={1} returns {2}")
        @CsvSource({
                "true, true, '[2026-02-13T22:34,2026-02-14T22:34]'",
                "false, false, '(2026-02-13T22:34,2026-02-14T22:34)'",
                "true, false, '[2026-02-13T22:34,2026-02-14T22:34)'",
                "false, true, '(2026-02-13T22:34,2026-02-14T22:34]'"
        })
        void toString_variousInclusivity_returnsCorrectNotation(boolean fromInclusive, boolean untilInclusive, String expected) {
            AbsoluteRange range = new AbsoluteRange(DATE_1, DATE_2, fromInclusive, untilInclusive);
            assertThat(range.toString()).isEqualTo(expected);
        }
    }

    @Nested
    class AbsoluteDateTest {
        @Test
        void toString_singleDate_returnsPointRange() {
            AbsoluteDate date = new AbsoluteDate(DATE_1, false, true);
            assertThat(date.toString()).isEqualTo("[2026-02-13T22:34,2026-02-13T22:34]");
        }
    }

    @Nested
    class FromAbsoluteDateTest {
        @ParameterizedTest(name = "toString with inclusive={0} returns {1}")
        @CsvSource({
                "true,  '[2026-02-13T22:34, ∞)'",
                "false, '(2026-02-13T22:34, ∞)'"
        })
        void toString_variousInclusivity_returnsCorrectNotation(boolean inclusive, String expected) {
            FromAbsoluteDate date = new FromAbsoluteDate(DATE_1, inclusive);
            assertThat(date.toString()).isEqualTo(expected);
        }
    }

    @Nested
    class UntilAbsoluteDateTest {
        @ParameterizedTest(name = "toString with inclusive={0} returns {1}")
        @CsvSource({
                "true,  '(-∞,2026-02-13T22:34]'",
                "false, '(-∞,2026-02-13T22:34)'"
        })
        void toString_variousInclusivity_returnsCorrectNotation(boolean inclusive, String expected) {
            UntilAbsoluteDate date = new UntilAbsoluteDate(DATE_1, inclusive);
            assertThat(date.toString()).isEqualTo(expected);
        }
    }
}
