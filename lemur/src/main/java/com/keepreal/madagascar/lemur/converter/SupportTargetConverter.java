package com.keepreal.madagascar.lemur.converter;

import com.keepreal.madagascar.coua.TargetType;
import com.keepreal.madagascar.coua.TimeType;
import swagger.model.SupportTargetTimeType;
import swagger.model.SupportTargetType;

import java.util.Objects;

/**
 * Represents a converter for {@link SupportTargetType} and {@link SupportTargetTimeType}
 */
public class SupportTargetConverter {

    /**
     * convert {@link SupportTargetType} to {@link TargetType}
     *
     * @param type {@link SupportTargetType}
     * @return {@link TargetType}
     */
    public static TargetType convertToTargetType(SupportTargetType type) {
        if (Objects.isNull(type)) {
            return null;
        }
        switch (type) {
            case SUPPORTER:
                return TargetType.SUPPORTER;
            case AMOUNT:
                return TargetType.AMOUNT;
            default:
                return TargetType.UNRECOGNIZED;
        }
    }

    /**
     * convert {@link TargetType} to {@link SupportTargetType}
     *
     * @param type {@link TargetType}
     * @return {@link SupportTargetType}
     */
    public static SupportTargetType convertToSupportTargetType(TargetType type) {
        if (Objects.isNull(type)) {
            return null;
        }
        switch (type) {
            case SUPPORTER:
                return SupportTargetType.SUPPORTER;
            case AMOUNT:
                return SupportTargetType.AMOUNT;
            default:
                return null;
        }
    }

    /**
     * convert {@link SupportTargetTimeType} to {@link TimeType}
     *
     * @param type {@link SupportTargetTimeType}
     * @return {@link TimeType}
     */
    public static TimeType convertToTimeType(SupportTargetTimeType type) {
        if (Objects.isNull(type)) {
            return null;
        }
        switch (type) {
            case NO_LIMIT:
                return TimeType.NO_LIMIT;
            case PER_MONTH:
                return TimeType.PER_MONTH;
            default:
                return TimeType.UNRECOGNIZED;
        }
    }

    /**
     * convert {@link TimeType} to {@link SupportTargetTimeType}
     *
     * @param type {@link TimeType}
     * @return {@link SupportTargetTimeType}
     */
    public static SupportTargetTimeType convertToSupportTargetTimeType(TimeType type) {
        if (Objects.isNull(type)) {
            return null;
        }
        switch (type) {
            case NO_LIMIT:
                return SupportTargetTimeType.NO_LIMIT;
            case PER_MONTH:
                return SupportTargetTimeType.PER_MONTH;
            default:
                return null;
        }
    }

}
