package com.keepreal.madagascar.mantella.service;

import com.google.protobuf.StringValue;
import com.keepreal.madagascar.common.IslandMessage;
import com.keepreal.madagascar.coua.IslandServiceGrpc;
import com.keepreal.madagascar.coua.IslandsResponse;
import com.keepreal.madagascar.coua.QueryIslandCondition;
import com.keepreal.madagascar.coua.RetrieveMultipleIslandsRequest;
import io.grpc.Channel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-06-22
 **/

@Service
public class TestIslandService {

    private final Channel couaChannel;

    public TestIslandService(@Qualifier("couaChannel") Channel couaChannel) {
        this.couaChannel = couaChannel;
    }

    public List<String> retrieveIslandIdsByUserId(String userId) {
        IslandServiceGrpc.IslandServiceBlockingStub stub = IslandServiceGrpc.newBlockingStub(couaChannel);

        IslandsResponse response = stub.retrieveIslandsByCondition(RetrieveMultipleIslandsRequest.newBuilder()
                .setCondition(QueryIslandCondition.newBuilder()
                        .setSubscribedUserId(StringValue.of(userId))
                        .build())
                .build());

        return response.getIslandsList().stream().map(IslandMessage::getId).collect(Collectors.toList());
    }
}
