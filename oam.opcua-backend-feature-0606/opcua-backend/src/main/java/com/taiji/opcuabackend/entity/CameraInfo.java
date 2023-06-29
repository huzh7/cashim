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
@TableName(value = "camera_info")
public class CameraInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("device_no")
    private String deviceNo;

    @TableField("point_name")
    private String pointName;

    @TableField("ip_addr")
    private String ipAddr;

    @TableField("mask")
    private String mask;

    @TableField("gateway")
    private String gateway;

    /**
     * 0: 正常
     * 1: 离线
     */
    @TableField("status")
    private Integer status;

    @TableField("room_key")
    private String roomKey;
}
