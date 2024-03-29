package com.keepreal.madagascar.lemur.dtoFactory;

import com.keepreal.madagascar.common.UserMessage;
import com.keepreal.madagascar.fossa.ReportMessage;
import org.springframework.stereotype.Component;
import swagger.model.ReportDTO;
import swagger.model.UserDTO;

import java.util.Objects;

/**
 * Represents the report dto factory.
 */
@Component
public class ReportDTOFactory {

    /**
     * Converts {@link UserMessage} to {@link UserDTO}.
     *
     * @param report {@link UserMessage}.
     * @return {@link UserDTO}.
     */
    public ReportDTO valueOf(ReportMessage report) {
        if (Objects.isNull(report)) {
            return null;
        }

        ReportDTO reportDTO = new ReportDTO();
        reportDTO.setId(report.getId());
        reportDTO.setReporterId(report.getReporterId());
        reportDTO.setType(report.getType().name());

        if (report.hasFeedId()) {
            reportDTO.setFeedId(report.getFeedId().getValue());
        }

        if (report.hasIslandId()) {
            reportDTO.setIslandId(report.getIslandId().getValue());
        }

        if (report.hasMessageId()) {
            reportDTO.setMessageId(report.getMessageId().getValue());
        }

        return reportDTO;
    }

}
