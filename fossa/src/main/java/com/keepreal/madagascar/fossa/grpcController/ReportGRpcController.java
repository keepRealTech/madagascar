package com.keepreal.madagascar.fossa.grpcController;

import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.fossa.NewReportRequest;
import com.keepreal.madagascar.fossa.ReportMessage;
import com.keepreal.madagascar.fossa.ReportResponse;
import com.keepreal.madagascar.fossa.ReportServiceGrpc;
import com.keepreal.madagascar.fossa.dao.ReportRepository;
import com.keepreal.madagascar.fossa.model.ReportInfo;
import com.keepreal.madagascar.fossa.util.CommonStatusUtils;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;

/**
 * Represents the report GRpc controller.
 */
@GRpcService
public class ReportGRpcController extends ReportServiceGrpc.ReportServiceImplBase {

    private final ReportRepository reportRepository;
    private final LongIdGenerator idGenerator;

    /**
     * Constructs report grpc controller.
     *
     * @param reportRepository  {@link ReportRepository}.
     * @param idGenerator       {@link LongIdGenerator}.
     */
    public ReportGRpcController(ReportRepository reportRepository,
                                LongIdGenerator idGenerator) {
        this.reportRepository = reportRepository;
        this.idGenerator = idGenerator;
    }

    /**
     * Implements create report method.
     *
     * @param request           {@link NewReportRequest}.
     * @param responseObserver  {@link ReportResponse}.
     */
    @Override
    public void createReport(NewReportRequest request, StreamObserver<ReportResponse> responseObserver) {
        String feedId = request.getFeedId();
        String reporterId = request.getReporterId();
        int typeValue = request.getTypeValue();
        ReportInfo reportInfo = reportRepository.findTopByFeedIdAndReporterIdAndTypeAndDeletedIsFalse(feedId, reporterId, typeValue);
        if (reportInfo == null) {
            reportInfo = ReportInfo.builder()
                    .id(String.valueOf(idGenerator.nextId()))
                    .feedId(feedId)
                    .reporterId(reporterId)
                    .type(typeValue)
                    .build();
            reportRepository.save(reportInfo);
        }

        ReportMessage reportMessage = ReportMessage.newBuilder()
                .setId(reportInfo.getId())
                .setFeedId(feedId)
                .setReporterId(reporterId)
                .setTypeValue(typeValue)
                .build();

        ReportResponse reportResponse = ReportResponse.newBuilder()
                .setReport(reportMessage)
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(reportResponse);
        responseObserver.onCompleted();
    }
}
