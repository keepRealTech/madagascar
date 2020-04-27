package com.keepreal.madagascar.coua.model;

import lombok.Data;

import javax.persistence.*;
import java.sql.Date;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-26
 **/

@Data
@Table(name = "user")
@Entity
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
    private Long createTime;
    private Long updateTime;
}
