package com.keepreal.madagascar.vanga.settlementCalculator;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Represents the default settlement timestamp calculator implementation.
 */
public class DefaultSettlementCalculator implements SettlementCalculator {

    /**
     * Generates the settlement day timestamp.
     *
     * @param currentDateTime Current date time
     * @param cycle           The cycle of settlement. One time payment cycle number is 0.
     * @return The valid timestamp for withdraw.
     */
    @Override
    public long generateSettlementTimestamp(ZonedDateTime currentDateTime, int cycle) {
        if (0 == cycle && currentDateTime.getDayOfMonth() >= 15) {
            return currentDateTime.plusMonths(1L)
                    .withDayOfMonth(15)
                    .truncatedTo(ChronoUnit.DAYS)
                    .toInstant()
                    .toEpochMilli();
        }

        return currentDateTime.plusMonths(cycle + 1)
                .withDayOfMonth(1)
                .truncatedTo(ChronoUnit.DAYS)
                .toInstant()
                .toEpochMilli();
    }

}
