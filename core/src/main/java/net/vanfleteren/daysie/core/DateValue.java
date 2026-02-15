package net.vanfleteren.daysie.core;

import org.jspecify.annotations.NonNull;

import java.time.LocalDateTime;

public sealed interface DateValue {

    record AbsoluteRange(LocalDateTime from, LocalDateTime until, boolean fromInclusive, boolean untilInclusive) implements DateValue{
        @Override
        public @NonNull String toString() {
            String startBracket = fromInclusive ? "[" : "(";
            String endBracket = untilInclusive ? "]" : ")";

            return String.format("%s%s,%s%s", startBracket, from, until, endBracket);
        }
    }

    record AbsoluteDate(LocalDateTime date, boolean isRangeBoundary, boolean isInclusive) implements DateValue {
        @Override
        public @NonNull String toString() {
            return String.format("[%s,%s]", date, date);
        }
    }

    record FromAbsoluteDate(LocalDateTime date, boolean inclusive) implements DateValue {
        @Override
        public @NonNull String toString() {
            String startBracket = inclusive ? "[" : "(";
            return String.format("%s%s, ∞)", startBracket, date);
        }
    }

    record UntilAbsoluteDate(LocalDateTime date, boolean inclusive) implements DateValue {
        @Override
        public @NonNull String toString() {
            String endBracket = inclusive ? "]" : ")";
            return String.format("(-∞,%s%s", date, endBracket);
        }
    }

}
