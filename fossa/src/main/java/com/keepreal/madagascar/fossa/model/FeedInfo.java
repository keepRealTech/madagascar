package com.keepreal.madagascar.fossa.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.EntityListeners;
import java.util.List;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-27
 **/

@Data
@Builder
@Document(value = "feedInfo")
@EntityListeners(AuditingEntityListener.class)
public class FeedInfo {

    @Id
    private String id;
    private String islandId;
    private String userId;
    private String text;
    private String hostId;
    private Boolean fromHost;
    private List<String> imageUrls;
    private List<String> membershipIds;
    private String duplicateTag;
    @Builder.Default
    private Integer likesCount = 0;
    @Builder.Default
    private Integer commentsCount = 0;
    @Builder.Default
    private Integer repostCount = 0;
    private Integer state;
    @Builder.Default
    private Boolean deleted = false;
    @CreatedDate
    private Long createdTime;
    @CreatedDate
    private Long toppedTime;
    @LastModifiedDate
    private Long updatedTime;

}
