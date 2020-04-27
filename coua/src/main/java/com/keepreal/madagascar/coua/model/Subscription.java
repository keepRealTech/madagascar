package com.keepreal.madagascar.coua.model;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-26
 **/

@Data
@Table(name = "subscription")
@Entity
public class Subscription {

    @Id
    private Long id;
    private Long userId;
    private Long islandId;
    private Integer state;
    private Integer number;
    private Long createTime;
    private Long updateTime;
}
