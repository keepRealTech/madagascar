package com.keepreal.madagascar.coua.service;

import com.keepreal.madagascar.common.snowflake.generator.LongIdGenerator;
import com.keepreal.madagascar.coua.QualificationMessage;
import com.keepreal.madagascar.coua.dao.UserQualificationRepository;
import com.keepreal.madagascar.coua.model.UserQualification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class UserQualificationService {

    private final UserQualificationRepository userQualificationRepository;
    private final LongIdGenerator idGenerator;

    public UserQualificationService(UserQualificationRepository userQualificationRepository,
                                    LongIdGenerator idGenerator) {
        this.userQualificationRepository = userQualificationRepository;
        this.idGenerator = idGenerator;
    }

    public List<UserQualification> retrieveUserQualificationsByUserId(String userId) {
        return this.userQualificationRepository.findUserQualificationsByUserIdAndDeletedIsFalseOrderByCreatedTime(userId);
    }

    public List<UserQualification> createOrUpdateQualifications(List<UserQualification> userQualifications) {
        userQualifications.forEach(userQualification -> {
            if (StringUtils.isEmpty(userQualification.getId())) {
                userQualification.setId(String.valueOf(idGenerator.nextId()));
            }
        });
        return userQualificationRepository.saveAll(userQualifications);
    }

    public QualificationMessage getMessage(UserQualification userQualification) {
        return QualificationMessage.newBuilder()
                .setId(userQualification.getId())
                .setName(userQualification.getName())
                .setUrl(userQualification.getHostUrl())
                .build();
    }
}
