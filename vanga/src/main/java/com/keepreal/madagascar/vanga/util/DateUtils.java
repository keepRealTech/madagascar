package com.keepreal.madagascar.vanga.util;


import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;

public class DateUtils {

    public static long startOfMonthTimestamp() {
        return startOfMonthTimestamp(0);
    }

    public static long startOfMonthTimestamp(int offsetMonth) {
        LocalDateTime startTime = LocalDateTime.now().plusMonths(offsetMonth).with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN);
        return startTime.atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    public static long startOfMonthTimestamp(long timestamp) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        LocalDateTime startTime = localDateTime.with(TemporalAdjusters.firstDayOfMonth()).with(LocalTime.MIN);
        return startTime.atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    public static long endOfMonthTimestamp() {
        return endOfMonthTimestamp(0);
    }

    public static long endOfMonthTimestamp(int offsetMonth) {
        LocalDateTime endTime = LocalDateTime.now().plusMonths(offsetMonth).with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX);
        return endTime.atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    public static long endOfMonthTimestamp(long timestamp) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        LocalDateTime endTime = localDateTime.with(TemporalAdjusters.lastDayOfMonth()).with(LocalTime.MAX);
        return endTime.atZone(ZoneId.systemDefault()).toEpochSecond();
    }
}
