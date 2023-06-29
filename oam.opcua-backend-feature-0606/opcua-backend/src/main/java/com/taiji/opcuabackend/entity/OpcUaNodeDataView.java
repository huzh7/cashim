package com.taiji.opcuabackend.entity;


import lombok.*;

import java.util.List;

/**
 * 映射XML文件相关标签及属性
 *
 */
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class OpcUaNodeDataView {

    // 对应XML文件中，data标签的id属性
    private List<OpcUaNodeDataView> folders;

    // 对应XML文件中，item标签中name属性
    private String name;

    // 对应XML文件中，item标签中access属性
    private String access;

    // 对应XML文件中，item标签中datatype属性
    private String datatype;

    // 对应XML文件中，item标签中initialValue属性
    private String initialValue;

    // 对应XML文件中，item标签中description属性
    private String description;

}
