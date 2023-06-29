package com.taiji.opcuabackend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "treatment_equipment")
public class TreatmentEquipment implements Serializable {
    /**
    * 主键
    */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
    * 治疗室
    */
    @TableField("treatment_room")
    private String treatmentRoom;

    /**
    * 治疗设备
    */
    @TableField("equipment")
    private String equipment;

    /**
     * 节点名称
     */
    @TableField("node_name")
    private String nodeName;


    // false为联锁，true为正常, null为未存在该节点订阅信息
    @TableField(exist = false)
    private String status;

    private static final long serialVersionUID = 1L;
}