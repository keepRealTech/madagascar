package com.keepreal.madagascar.fossa.service;

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
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-27
 **/

@GRpcService
public class ReportService extends ReportServiceGrpc.ReportServiceImplBase {

    private final ReportRepository reportRepository;
    private final LongIdGenerator idGenerator;

    @Autowired
    public ReportService(ReportRepository reportRepository, LongIdGenerator idGenerator) {
        this.reportRepository = reportRepository;
        this.idGenerator = idGenerator;
    }

    /**
     * 根据id举报一个feed
     * @param request
     * @param responseObserver
     */
    @Override
    public void createReport(NewReportRequest request, StreamObserver<ReportResponse> responseObserver) {
        String feedId = request.getFeedId();
        String reporterId = request.getReporterId();
        int typeValue = request.getTypeValue();
        ReportInfo reportInfo = new ReportInfo();
        reportInfo.setId(idGenerator.nextId());
        reportInfo.setFeedId(Long.valueOf(feedId));
        reportInfo.setReporterId(Long.valueOf(reporterId));
        reportInfo.setType(typeValue);
        reportInfo.setCreatedTime(System.currentTimeMillis());
        reportInfo.setUpdatedTime(System.currentTimeMillis());

        ReportInfo save = reportRepository.save(reportInfo);

        ReportMessage reportMessage = ReportMessage.newBuilder()
                .setId(save.getId().toString())
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
