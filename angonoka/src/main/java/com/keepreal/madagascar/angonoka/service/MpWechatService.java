package com.keepreal.madagascar.angonoka.service;

import com.keepreal.madagascar.common.exceptions.ErrorCode;
import com.keepreal.madagascar.common.exceptions.KeepRealBusinessException;
import com.keepreal.madagascar.hawksbill.MpWechatServiceGrpc;
import com.keepreal.madagascar.hawksbill.SendTemplateMessageRequest;
import com.keepreal.madagascar.hawksbill.SendTemplateMessageResponse;
import io.grpc.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class MpWechatService {
    private final Channel channel;

    public MpWechatService(@Qualifier("hawksbillChannel") Channel channel) {
        this.channel = channel;
    }

    /**
     * 发送模板消息
     *
     * @param openIds follower open id
     */
    public void sendTemplateMessageByOpenIds(List<String> openIds, String name, String url, String text) {
        MpWechatServiceGrpc.MpWechatServiceBlockingStub stub = MpWechatServiceGrpc.newBlockingStub(this.channel);
        SendTemplateMessageRequest request = SendTemplateMessageRequest.newBuilder()
                .setName(name)
                .setText(text)
                .setUrl(url)
                .addAllOpenIds(openIds)
                .build();
        SendTemplateMessageResponse response;
        try {
            response = stub.sendTemplateMessage(request);
        } catch (Exception e) {
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (Objects.isNull(response)
                || !response.hasStatus()) {
            log.error(Objects.isNull(response) ? "Send template message returned null." : response.toString());
            throw new KeepRealBusinessException(ErrorCode.REQUEST_UNEXPECTED_ERROR);
        }

        if (ErrorCode.REQUEST_SUCC_VALUE != response.getStatus().getRtn()) {
            throw new KeepRealBusinessException(response.getStatus());
        }
    }

}
