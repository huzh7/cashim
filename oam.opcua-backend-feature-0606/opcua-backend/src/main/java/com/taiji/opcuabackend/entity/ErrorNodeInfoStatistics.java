package com.taiji.opcuabackend.entity;

import lombok.*;

import java.io.Serializable;
import java.util.Date;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ErrorNodeInfoStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String node1;

    private String node2;

    private String node3;

    private String node4;

    private String nodeValue;

    private Date updateTime;

    private Date createTime;

    private int status;

    private int cnt;
}
