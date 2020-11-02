package com.keepreal.madagascar.lemur.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import swagger.model.IncomeType;

@Component
public class IncomeTypeConverter implements Converter<String, IncomeType> {

    @Override
    public IncomeType convert(String source) {
        if ("null".equals(source)) {
            return null;
        }

        if ("month".equals(source.toLowerCase())) {
            return IncomeType.MONTH;
        }

        if ("feed_charge".equals(source.toLowerCase())) {
            return IncomeType.FEED_CHARGE;
        }

        if ("support".equals(source.toLowerCase())) {
            return IncomeType.SUPPORT;
        }

        if ("membership".equals(source.toLowerCase())) {
            return IncomeType.MEMBERSHIP;
        }

        return IncomeType.fromValue(source);
    }
}
