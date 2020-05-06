package com.keepreal.madagascar.fossa.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import java.util.List;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-27
 **/

@Data
@Document(value = "feedInfo")
@EntityListeners(AuditingEntityListener.class)
public class FeedInfo {

    @Id
    private Long id;
    private Long islandId;
    private Long userId;
    private String text;
    private List<String> imageUrls;
    private Integer likesCount;
    private Integer commentsCount;
    private Integer repostCount;
    private Integer state;
    @Column(name = "is_deleted")
    private boolean deleted;
    @CreatedDate
    private Long createdTime;
    @LastModifiedDate
    private Long updatedTime;
}
