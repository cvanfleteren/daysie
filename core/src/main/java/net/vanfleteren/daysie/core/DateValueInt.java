package net.vanfleteren.daysie.core;

import org.jspecify.annotations.NonNull;

import java.time.LocalDateTime;

/**
 * Internal representation of Range/Date
 */
sealed interface DateValueInt {

    DateValue toPublic();

    record AbsoluteRange(LocalDateTime from, LocalDateTime until, boolean fromInclusive, boolean untilInclusive) implements DateValueInt {
        @Override
        public @NonNull String toString() {
            String startBracket = fromInclusive ? "[" : "(";
            String endBracket = untilInclusive ? "]" : ")";

            String fromStr = from.equals(LocalDateTime.MIN) ? "-∞" : from.toString();
            String untilStr = until.equals(LocalDateTime.MAX) ? " ∞" : until.toString();

            return String.format("%s%s,%s%s", startBracket, fromStr, untilStr, endBracket);
        }

        public DateValue.AbsoluteRange toPublic() {
            return DateValue.AbsoluteRange.from(this);
        }
    }

    record AbsoluteDateInt(LocalDateTime date, boolean isRangeBoundary, boolean isInclusive) implements DateValueInt {
        @Override
        public @NonNull String toString() {
            return String.format("[%s,%s]", date, date);
        }

        public DateValue.AbsoluteDate toPublic() {
            return DateValue.AbsoluteDate.from(this);
        }
    }

}
