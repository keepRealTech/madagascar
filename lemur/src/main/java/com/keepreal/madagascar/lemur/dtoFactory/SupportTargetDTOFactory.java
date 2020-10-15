package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.coua.SupportTargetMessage;
import com.keepreal.madagascar.lemur.converter.SupportTargetConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import swagger.model.SupportTargetDTO;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * Represents the support target dto factory.
 */
@Component
public class SupportTargetDTOFactory {

    /**
     * converts {@link SupportTargetMessage} to {@link SupportTargetDTO}
     *
     * @param message {@link SupportTargetMessage}
     * @return {@link SupportTargetDTO}
     */
    public SupportTargetDTO valueOf(SupportTargetMessage message) {
        if (Objects.isNull(message)) {
            return null;
        }

        SupportTargetDTO supportTargetDTO = new SupportTargetDTO();
        supportTargetDTO.setTargetId(message.getId());
        supportTargetDTO.setTargetType(SupportTargetConverter.convertToSupportTargetType(message.getTargetType()));
        supportTargetDTO.setTimeType(SupportTargetConverter.convertToSupportTargetTimeType(message.getTimeType()));
        supportTargetDTO.setContent(message.getContent());
        switch (message.getTargetType()) {
            case SUPPORTER:
                supportTargetDTO.setCurrentSupporterNum(message.getCurrentSupporterNum());
                supportTargetDTO.setTotalSupporterNum(message.getTotalSupporterNum());
                supportTargetDTO.setCompleted(message.getCurrentSupporterNum() >= message.getTotalSupporterNum());
                break;
            case AMOUNT:
                supportTargetDTO.setCurrentAmountInCents(message.getCurrentAmountInCents());
                supportTargetDTO.setTotalAmountInCents(message.getTotalAmountInCents());
                supportTargetDTO.setCompleted(message.getCurrentAmountInCents() >= message.getTotalAmountInCents());
                break;
        }
        return supportTargetDTO;
    }

    /**
     * converts {@link List<SupportTargetMessage>} to the {@link List<SupportTargetDTO>}
     *
     * @param messages {@link List<SupportTargetMessage>}
     * @return {@link List<SupportTargetDTO>}
     */
    public List<SupportTargetDTO> listValueOf (List<SupportTargetMessage> messages) {
        if (CollectionUtils.isEmpty(messages)) {
            return null;
        }
        return messages.stream().map(this::valueOf).collect(Collectors.toList());
    }

}
