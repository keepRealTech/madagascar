package com.keepreal.madagascar.fossa.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @program: madagascar
 * @author: zhangxidong
 * @create: 2020-04-27
 **/

@Data
@Entity
@Table(name = "report")
public class ReportInfo {

    @Id
    private Long id;
    private Integer type;
    private Long feedId;
    private Long reporterId;
    @Column(name = "is_deleted")
    private Boolean deleted;
    @CreatedDate
    private Long createdTime;
    @LastModifiedDate
    private Long updatedTime;
}
