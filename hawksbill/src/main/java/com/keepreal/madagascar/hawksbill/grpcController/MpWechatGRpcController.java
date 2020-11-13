package com.keepreal.madagascar.hawksbill.grpcController;

import com.google.protobuf.ProtocolStringList;
import com.keepreal.madagascar.common.EmptyMessage;
import com.keepreal.madagascar.hawksbill.MpWechatServiceGrpc;
import com.keepreal.madagascar.hawksbill.RetrievePermanentQRCodeResponse;
import com.keepreal.madagascar.hawksbill.SendMsgToUserByOpenIdRequest;
import com.keepreal.madagascar.hawksbill.SendMsgToUserByOpenIdResponse;
import com.keepreal.madagascar.hawksbill.SendTemplateMessageRequest;
import com.keepreal.madagascar.hawksbill.SendTemplateMessageResponse;
import com.keepreal.madagascar.hawksbill.service.MpWechatService;
import com.keepreal.madagascar.hawksbill.util.CommonStatusUtils;
import io.grpc.stub.StreamObserver;
import org.lognet.springboot.grpc.GRpcService;

/**
 * Represents the mp wechat grpc controller.
 */
@GRpcService
public class MpWechatGRpcController extends MpWechatServiceGrpc.MpWechatServiceImplBase {

    private final MpWechatService mpWechatService;

    public MpWechatGRpcController(MpWechatService mpWechatService) {
        this.mpWechatService = mpWechatService;
    }

    @Override
    public void sendTemplateMessage(SendTemplateMessageRequest request, StreamObserver<SendTemplateMessageResponse> responseObserver) {
        String name = request.getName();
        String text = request.getText();
        String url = request.getUrl();
        ProtocolStringList openIdsList = request.getOpenIdsList();
        openIdsList.forEach(openId -> {
            this.mpWechatService.sendTemplateMessageByOpenId(openId, name, url, text);
        });
        responseObserver.onNext(SendTemplateMessageResponse.newBuilder().setStatus(CommonStatusUtils.getSuccStatus()).build());
        responseObserver.onCompleted();
    }

    @Override
    public void retrievePermanentQRCode(EmptyMessage request, StreamObserver<RetrievePermanentQRCodeResponse> responseObserver) {
        String ticket = this.mpWechatService.retrievePermanentQRCode();
        responseObserver.onNext(RetrievePermanentQRCodeResponse.newBuilder()
                .setStatus(CommonStatusUtils.getSuccStatus())
                .setTicket(ticket)
                .build());
        responseObserver.onCompleted();
    }

}
