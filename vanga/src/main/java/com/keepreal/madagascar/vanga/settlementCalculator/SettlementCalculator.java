package com.keepreal.madagascar.vanga.settlementCalculator;

import java.time.ZonedDateTime;

/**
 * Represents the settlement day calculator.
 */
public interface SettlementCalculator {

    long generateSettlementTimestamp(ZonedDateTime currentTimestamp, int cycle);

}
