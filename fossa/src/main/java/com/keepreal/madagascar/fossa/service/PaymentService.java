package com.keepreal.madagascar.fossa.service;

import io.grpc.Channel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class PaymentService {

    private final Channel channel;

    public PaymentService(@Qualifier("vangaChannel") Channel channel) {
        this.channel = channel;
    }
}
