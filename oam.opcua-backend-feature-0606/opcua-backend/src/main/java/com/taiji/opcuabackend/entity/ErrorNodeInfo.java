package com.taiji.opcuabackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;
import java.util.Date;


@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "error_node_info")
public class ErrorNodeInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("node1")
    private String node1;

    @TableField("node2")
    private String node2;

    @TableField("node3")
    private String node3;

    @TableField("node4")
    private String node4;

    @TableField("node_value")
    private String nodeValue;

    @TableField("update_time")
    private Date updateTime;

    @TableField("create_time")
    private Date createTime;

    @TableField("status")
    private int status;
}
