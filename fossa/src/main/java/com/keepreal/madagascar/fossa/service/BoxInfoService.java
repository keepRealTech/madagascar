package com.keepreal.madagascar.fossa.service;

import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.fossa.BoxMessage;
import com.keepreal.madagascar.fossa.dao.BoxInfoRepository;
import com.keepreal.madagascar.fossa.model.BoxInfo;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class BoxInfoService {

    private final BoxInfoRepository boxInfoRepository;
    private final LongIdGenerator idGenerator;

    public BoxInfoService(BoxInfoRepository boxInfoRepository,
                          LongIdGenerator idGenerator) {
        this.boxInfoRepository = boxInfoRepository;
        this.idGenerator = idGenerator;
    }

    public BoxInfo createOrUpdate(BoxInfo boxInfo) {
        boxInfo.setId(String.valueOf(idGenerator.nextId()));

        return boxInfoRepository.save(boxInfo);
    }

    public BoxMessage getBoxMessage(BoxInfo boxInfo) {
        if (boxInfo == null) {
            return null;
        }

        return BoxMessage.newBuilder()
                .setId(boxInfo.getId())
                .setIsland(boxInfo.getIslandId())
                .setEnabled(boxInfo.isEnabled())
                .addAllMembershipIds(Arrays.asList(boxInfo.getMembershipIds().split(",")))
                .build();
    }
}
