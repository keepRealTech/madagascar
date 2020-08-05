package com.keepreal.madagascar.coua.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.Table;
import java.sql.Date;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-26
 **/

@Data
@Table(name = "user")
@Entity
@EntityListeners(AuditingEntityListener.class)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {

    @Id
    private String id;
    private String displayId;
    private String nickName;
    private String portraitImageUri;
    @Builder.Default
    private String username = "";
    @Builder.Default
    private String password = "";
    @Builder.Default
    private Integer gender = 0;
    @Builder.Default
    private String description = "";
    @Builder.Default
    private String city = "";
    @Builder.Default
    private Date birthday = Date.valueOf("2000-01-01");
    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean deleted = false;
    @Builder.Default
    private Long lockedUntil = 0L;
    @Builder.Default
    private Integer state = 0;
    private String unionId;
    @CreatedDate
    private Long createdTime;
    @LastModifiedDate
    private Long updatedTime;
    @Builder.Default
    private Boolean shouldIntroduce = true;
}
