package com.keepreal.madagascar.lemur.service;

import com.keepreal.madagascar.common.ReportType;
import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.fossa.NewReportRequest;
import com.keepreal.madagascar.fossa.ReportMessage;
import com.keepreal.madagascar.fossa.ReportResponse;
import com.keepreal.madagascar.fossa.ReportServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Represents the report service.
 */
@Service
@Slf4j
public class ReportService {

    private final ManagedChannel managedChannel;

    /**
     * Constructs the report service.
     *
     * @param managedChannel GRpc managed channel connection to service coua.
     */
    public ReportService(@Qualifier("fossaChannel") ManagedChannel managedChannel) {
        this.managedChannel = managedChannel;
    }

    /**
     * Creates a new report.
     *
     * @param feedId Feed id.
     * @param userId User id.
     * @param type   {@link ReportType}.
     * @return {@link ReportMessage}.
     */
    public ReportMessage createReport(String feedId, String userId, ReportType type) {
        ReportServiceGrpc.ReportServiceBlockingStub stub = ReportServiceGrpc.newBlockingStub(this.managedChannel);

        NewReportRequest request = NewReportRequest.newBuilder()
                .setFeedId(feedId)
                .setReporterId(userId)
                .setType(type)
                .build();

        ReportResponse reportResponse;
        try {
            reportResponse = stub.createReport(request);
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
