package com.keepreal.madagascar.fossa.grpcController;

import com.google.protobuf.StringValue;
import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.fossa.NewReportRequest;
import com.keepreal.madagascar.fossa.Report;
import com.keepreal.madagascar.fossa.ReportMessage;
import com.keepreal.madagascar.fossa.ReportResponse;
import com.keepreal.madagascar.fossa.ReportServiceGrpc;
import com.keepreal.madagascar.fossa.dao.ReportRepository;
import com.keepreal.madagascar.fossa.model.ReportInfo;
import com.keepreal.madagascar.fossa.util.CommonStatusUtils;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;
import org.springframework.util.StringUtils;

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
        ReportInfo.ReportInfoBuilder reportInfoBuilder = ReportInfo.builder()
                .id(String.valueOf(idGenerator.nextId()))
                .type(request.getTypeValue());

        if (request.hasFeedId()) {
            reportInfoBuilder.feedId(request.getFeedId().getValue());
        } else {
            reportInfoBuilder.islandId(request.getIslandId().getValue());
        }

        ReportInfo reportInfo = reportRepository.save(reportInfoBuilder.build());

        ReportMessage.Builder reportMessageBuilder = ReportMessage.newBuilder()
                .setId(reportInfo.getId())
                .setTypeValue(request.getTypeValue());

        if (!StringUtils.isEmpty(reportInfo.getFeedId())) {
            reportMessageBuilder.setFeedId(StringValue.of(reportInfo.getFeedId()));
        } else {
            reportMessageBuilder.setIslandId(StringValue.of(reportInfo.getIslandId()));
        }

        ReportResponse reportResponse = ReportResponse.newBuilder()
                .setReport(reportMessageBuilder.build())
                .setStatus(CommonStatusUtils.getSuccStatus())
                .build();
        responseObserver.onNext(reportResponse);
        responseObserver.onCompleted();
    }

}
