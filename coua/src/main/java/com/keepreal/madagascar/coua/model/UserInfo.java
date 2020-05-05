package com.keepreal.madagascar.coua.model;

import lombok.Data;
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
public class UserInfo {

    @Id
    private Long id;
    private String nickName;
    private String portraitImageUri;
    private Integer gender;
    private String description;
    private String city;
    private Date birthday;
    @Column(name = "is_deleted")
    private Boolean deleted;
    private Integer state;
    private String unionId;
    @CreatedDate
    private Long createdTime;
    @LastModifiedDate
    private Long updatedTime;
}
