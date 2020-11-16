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
    @Builder.Default
    private String title = "";
    @Builder.Default
    private String brief = "";
    @Builder.Default
    private String text = "";
    private String hostId;
    private Boolean fromHost;
    private List<String> imageUrls;
    private List<String> membershipIds;
    /**
     * Used by question. 提问者的用户状态.
     */
    private List<String> userMembershipIds;
    private String duplicateTag;
    private String multiMediaType;
    private List<MediaInfo> mediaInfos;
    private String feedGroupId;
    @Builder.Default
    private Long priceInCents = 0L;
    @Builder.Default
    private Integer likesCount = 0;
    @Builder.Default
    private Integer commentsCount = 0;
    @Builder.Default
    private Integer repostCount = 0;
    private Integer state;
    @Builder.Default
    private Boolean isWorks = false;
    @Builder.Default
    private Boolean deleted = false;
    @Builder.Default
    private Boolean isTop = false;
    @Builder.Default
    private Boolean temped = false;
    @Builder.Default
    private Boolean canSave = false;
    @CreatedDate
    private Long createdTime;
    @CreatedDate
    private Long toppedTime;
    @LastModifiedDate
    private Long updatedTime;

}
