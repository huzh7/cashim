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
@TableName(value = "client_info")
public class ClientInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 客户端
     */
    @TableField("client")
    private String client;

    @TableField("ip_addr")
    private String ipAddr;

    @TableField("token")
    private String token;

    @TableField("register_date")
    private Date registerDate;

    /**
     * 0:正常 1:过期 2:禁用
     */
    @TableField("status")
    private int status;

    @TableField("is_channel_on")
    private Integer isChannelOn;
}
