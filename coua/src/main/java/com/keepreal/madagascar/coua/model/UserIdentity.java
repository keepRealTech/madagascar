package com.keepreal.madagascar.coua.model;

import lombok.Data;
import javax.persistence.*;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-26
 **/

@Data
@Table(name = "user_identity")
@Entity
public class UserIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Integer identityType;
    private Boolean deleted;
    private Long createdTime;
    private Long updatedTime;
}
