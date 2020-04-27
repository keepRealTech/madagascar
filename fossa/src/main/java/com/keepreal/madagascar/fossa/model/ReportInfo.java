package com.keepreal.madagascar.fossa.model;

import lombok.Data;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-27
 **/

@Data
public class ReportInfo {

    private Long id;
    private Integer type;
    private Long feedId;
    private Long reporterId;
    private Long createTime;
    private Long updateTime;
}
