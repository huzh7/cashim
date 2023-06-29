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
public class OpcUaXMLDataView {

    // 对应XML文件中，item标签中name属性
    private String name;

    // 对应XML文件中，item标签中port属性
    private String port;

    // 对应XML文件中，item标签中serverCertification属性
    private String serverCertification;

    // 对应XML文件中，item标签中trustedClientCertifications属性
    private List<String> trustedClientCertifications;

    // 对应XML文件中，item标签中description属性
    private String description;

    // 对应XML文件中，item标签中structure属性
    private List<OpcUaNodeDataView> structure;


}
