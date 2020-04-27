package com.keepreal.madagascar.fossa.service;

import com.keepreal.madagascar.fossa.NewReportRequest;
import com.keepreal.madagascar.fossa.ReportResponse;
import com.keepreal.madagascar.fossa.ReportServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-27
 **/

@GRpcService
public class ReportService extends ReportServiceGrpc.ReportServiceImplBase {

    @Override
    public void createReport(NewReportRequest request, StreamObserver<ReportResponse> responseObserver) {
        super.createReport(request, responseObserver);
    }
}
