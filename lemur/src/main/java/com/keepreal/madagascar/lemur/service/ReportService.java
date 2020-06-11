package com.keepreal.madagascar.lemur.service;

import com.google.protobuf.StringValue;
import com.keepreal.madagascar.common.ReportType;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.fossa.NewReportRequest;
import com.keepreal.madagascar.fossa.ReportMessage;
import com.keepreal.madagascar.fossa.ReportResponse;
import com.keepreal.madagascar.fossa.ReportServiceGrpc;
import io.grpc.Channel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * Represents the report service.
 */
@Service
@Slf4j
public class ReportService {

    private final Channel channel;

    /**
     * Constructs the report service.
     *
     * @param channel GRpc managed channel connection to service coua.
     */
    public ReportService(@Qualifier("fossaChannel") Channel channel) {
        this.channel = channel;
    }

    /**
     * Creates a new report.
     *
     * @param feedId     Feed id.
     * @param islandId   Island id.
     * @param userId     User id.
     * @param reporterId Reporter id.
     * @param type       {@link ReportType}.
     * @return {@link ReportMessage}.
     */
    public ReportMessage createReport(String feedId, String islandId, String userId, String reporterId, ReportType type) {
        ReportServiceGrpc.ReportServiceBlockingStub stub = ReportServiceGrpc.newBlockingStub(this.channel);

        NewReportRequest.Builder requestBuilder = NewReportRequest.newBuilder()
                .setReporterId(reporterId)
                .setType(type);

        if (!StringUtils.isEmpty(feedId)) {
            requestBuilder.setFeedId(StringValue.of(feedId));
        } else if (!StringUtils.isEmpty(islandId)) {
            requestBuilder.setFeedId(StringValue.of(islandId));
        } else {
            requestBuilder.setUserId(StringValue.of(userId));
        }

        ReportResponse reportResponse;
        try {
            reportResponse = stub.createReport(requestBuilder.build());
        } catch (StatusRuntimeException exception) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR, exception.getMessage());
        }

        if (Objects.isNull(reportResponse)
                || !reportResponse.hasStatus()) {
            log.error(Objects.isNull(reportResponse) ? "Create report returned null." : reportResponse.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != reportResponse.getStatus().getRtn()) {
            throw new KeepRealBusinessException(reportResponse.getStatus());
        }

        return reportResponse.getReport();
    }

}
