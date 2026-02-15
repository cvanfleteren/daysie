package net.vanfleteren.daysie.core;

import org.jspecify.annotations.NonNull;

import java.time.LocalDateTime;

public sealed interface DateValue {

    record AbsoluteRange(LocalDateTime from, LocalDateTime until, boolean fromInclusive, boolean untilInclusive) implements DateValue{
        @Override
        public @NonNull String toString() {
            String startBracket = fromInclusive ? "[" : "(";
            String endBracket = untilInclusive ? "]" : ")";

            String fromStr = from.equals(LocalDateTime.MIN) ? "-∞" : from.toString();
            String untilStr = until.equals(LocalDateTime.MAX) ? " ∞" : until.toString();

            return String.format("%s%s,%s%s", startBracket, fromStr, untilStr, endBracket);
        }
    }

    record AbsoluteDate(LocalDateTime date, boolean isRangeBoundary, boolean isInclusive) implements DateValue {
        @Override
        public @NonNull String toString() {
            return String.format("[%s,%s]", date, date);
        }
    }

}
