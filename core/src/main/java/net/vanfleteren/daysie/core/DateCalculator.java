package net.vanfleteren.daysie.core;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

class DateCalculator {

    static DateValue calculateLastRange(LocalDateTime now, ChronoUnit unit, int amount, boolean isQuarter) {
        LocalDate today = now.toLocalDate();
        return switch (unit) {
            case SECONDS -> {
                LocalDateTime start = now.minusSeconds(amount);
                yield new DateValue.AbsoluteRange(start, now, true, false);
            }
            case MINUTES -> {
                LocalDateTime start = now.minusMinutes(amount);
                yield new DateValue.AbsoluteRange(start, now, true, false);
            }
            case HOURS -> {
                LocalDateTime start = now.minusHours(amount);
                yield new DateValue.AbsoluteRange(start, now, true, false);
            }
            case DAYS -> {
                LocalDate start = today.minusDays(amount);
                yield new DateValue.AbsoluteRange(start.atStartOfDay(), now, true, false);
            }
            case WEEKS -> {
                LocalDate startOfThisWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                LocalDate start = startOfThisWeek.minusWeeks(amount);
                yield new DateValue.AbsoluteRange(start.atStartOfDay(), startOfThisWeek.atStartOfDay(), true, false);
            }
            case MONTHS -> {
                if (isQuarter) {
                    LocalDate startOfThisQuarter = getStartOfQuarter(today);
                    LocalDate start = startOfThisQuarter.minusMonths(amount * 3L);
                    yield new DateValue.AbsoluteRange(start.atStartOfDay(), startOfThisQuarter.atStartOfDay(), true, false);
                } else {
                    LocalDate startOfThisMonth = today.with(TemporalAdjusters.firstDayOfMonth());
                    LocalDate start = startOfThisMonth.minusMonths(amount);
                    yield new DateValue.AbsoluteRange(start.atStartOfDay(), startOfThisMonth.atStartOfDay(), true, false);
                }
            }
            case YEARS -> {
                LocalDate startOfThisYear = today.with(TemporalAdjusters.firstDayOfYear());
                LocalDate start = startOfThisYear.minusYears(amount);
                yield new DateValue.AbsoluteRange(start.atStartOfDay(), startOfThisYear.atStartOfDay(), true, false);
            }
            default -> null;
        };
    }

    static DateValue calculateThisRange(LocalDateTime now, ChronoUnit unit, int amount, boolean isQuarter) {
        LocalDate today = now.toLocalDate();
        return switch (unit) {
            case SECONDS -> {
                LocalDateTime start = now.minusSeconds(amount - 1L).withNano(0);
                yield new DateValue.AbsoluteRange(start, start.plusSeconds(1), true, false);
            }
            case MINUTES -> {
                LocalDateTime start = now.minusMinutes(amount - 1L).withNano(0).withSecond(0);
                yield new DateValue.AbsoluteRange(start, start.plusMinutes(1), true, false);
            }
            case HOURS -> {
                LocalDateTime start = now.minusHours(amount - 1L).withNano(0).withSecond(0).withMinute(0);
                yield new DateValue.AbsoluteRange(start, start.plusHours(1), true, false);
            }
            case DAYS -> {
                LocalDate start = today.minusDays(amount - 1L);
                yield new DateValue.AbsoluteRange(start.atStartOfDay(), today.plusDays(1).atStartOfDay(), true, false);
            }
            case WEEKS -> {
                LocalDate startOfThisWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                LocalDate start = startOfThisWeek.minusWeeks(amount - 1L);
                yield new DateValue.AbsoluteRange(start.atStartOfDay(), startOfThisWeek.plusWeeks(1).atStartOfDay(), true, false);
            }
            case MONTHS -> {
                if (isQuarter) {
                    LocalDate startOfThisQuarter = getStartOfQuarter(today);
                    LocalDate start = startOfThisQuarter.minusMonths((amount - 1L) * 3);
                    yield new DateValue.AbsoluteRange(start.atStartOfDay(), startOfThisQuarter.plusMonths(3).atStartOfDay(), true, false);
                } else {
                    LocalDate startOfThisMonth = today.with(TemporalAdjusters.firstDayOfMonth());
                    LocalDate start = startOfThisMonth.minusMonths(amount - 1L);
                    yield new DateValue.AbsoluteRange(start.atStartOfDay(), startOfThisMonth.plusMonths(1).atStartOfDay(), true, false);
                }
            }
            case YEARS -> {
                LocalDate startOfThisYear = today.with(TemporalAdjusters.firstDayOfYear());
                LocalDate start = startOfThisYear.minusYears(amount - 1L);
                yield new DateValue.AbsoluteRange(start.atStartOfDay(), startOfThisYear.plusYears(1).atStartOfDay(), true, false);
            }
            default -> null;
        };
    }

    static DateValue calculateNextRange(LocalDateTime now, ChronoUnit unit, int amount, boolean isQuarter) {
        LocalDate today = now.toLocalDate();
        return switch (unit) {
            case SECONDS -> {
                LocalDateTime end = now.plusSeconds(amount);
                yield new DateValue.AbsoluteRange(now, end, true, false);
            }
            case MINUTES -> {
                LocalDateTime end = now.plusMinutes(amount);
                yield new DateValue.AbsoluteRange(now, end, true, false);
            }
            case HOURS -> {
                LocalDateTime end = now.plusHours(amount);
                yield new DateValue.AbsoluteRange(now, end, true, false);
            }
            case DAYS -> {
                yield new DateValue.AbsoluteRange(now, today.plusDays(amount + 1L).atStartOfDay(), true, false);
            }
            case WEEKS -> {
                LocalDate startOfNextWeek = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
                yield new DateValue.AbsoluteRange(startOfNextWeek.atStartOfDay(), startOfNextWeek.plusWeeks(amount).atStartOfDay(), true, false);
            }
            case MONTHS -> {
                if (isQuarter) {
                    LocalDate startOfNextQuarter = getStartOfQuarter(today).plusMonths(3);
                    yield new DateValue.AbsoluteRange(startOfNextQuarter.atStartOfDay(), startOfNextQuarter.plusMonths(amount * 3L).atStartOfDay(), true, false);
                } else {
                    LocalDate startOfNextMonth = today.with(TemporalAdjusters.firstDayOfMonth()).plusMonths(1);
                    yield new DateValue.AbsoluteRange(startOfNextMonth.atStartOfDay(), startOfNextMonth.plusMonths(amount).atStartOfDay(), true, false);
                }
            }
            case YEARS -> {
                LocalDate startOfNextYear = today.with(TemporalAdjusters.firstDayOfYear()).plusYears(1);
                yield new DateValue.AbsoluteRange(startOfNextYear.atStartOfDay(), startOfNextYear.plusYears(amount).atStartOfDay(), true, false);
            }
            default -> null;
        };
    }

    static LocalDate getStartOfQuarter(LocalDate date) {
        int currentMonth = date.getMonthValue();
        int startMonthOfQuarter = ((currentMonth - 1) / 3) * 3 + 1;
        return date.withMonth(startMonthOfQuarter).withDayOfMonth(1);
    }

    static DateValue calculateAgoDate(LocalDateTime now, ChronoUnit unit, int amount) {
        return new DateValue.AbsoluteDate(now.minus(amount, unit), false, true);
    }

    static DateValue calculateFromNowDate(LocalDateTime now, ChronoUnit unit, int amount) {
        return new DateValue.AbsoluteDate(now.plus(amount, unit), false, true);
    }

    static DateValue calculateDayOfWeekAgo(LocalDateTime now, DayOfWeek dayOfWeek, int amount) {
        LocalDate today = now.toLocalDate();
        LocalDate target = today.with(TemporalAdjusters.previousOrSame(dayOfWeek)).minusWeeks(amount - 1L);
        return new DateValue.AbsoluteDate(target.atStartOfDay(), false, true);
    }

    static DateValue calculateDayOfWeekFromNow(LocalDateTime now, DayOfWeek dayOfWeek, int amount) {
        LocalDate today = now.toLocalDate();
        LocalDate target = today.with(TemporalAdjusters.nextOrSame(dayOfWeek)).plusWeeks(amount - 1L);
        return new DateValue.AbsoluteDate(target.atStartOfDay(), false, true);
    }
}
