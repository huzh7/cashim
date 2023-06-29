package com.taiji.opcuabackend.entity;


import lombok.*;

/**
 * 映射XML文件Item相关节点以及属性
 *
 */
@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class OpcUaItemDataView {

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

    // 对应XML文件中，item标签中identifier属性
    private String identifier;

    // 是否在opcua server节点中出现，true为出现，false未在opuca server节点中出现;默认值为false
    private boolean isExistOpcUaServer = false;

    // 是否在opcua server节点中出现，true为出现，false未在opuca server节点中出现
    private int namespaceIndex;

    //订阅的节点value
    private String nodeValue;

}
