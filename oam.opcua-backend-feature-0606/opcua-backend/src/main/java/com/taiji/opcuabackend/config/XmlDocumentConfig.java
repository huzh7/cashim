package com.taiji.opcuabackend.config;

import com.taiji.opcuabackend.entity.OpcUaItemDataView;
import com.taiji.opcuabackend.entity.OpcUaXMLDataView;
import com.taiji.opcuabackend.util.Dom4jUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.List;

/**
 * Xml文件加载类
 */

@Slf4j
@Component
public class XmlDocumentConfig {

    @Autowired
    private OpcUaConfig opcUaConfig;
    @Autowired
    private Dom4jUtil dom4jUtil;

    //静态变量用于存储服务启动是加载的Xml文件节点
    public static OpcUaXMLDataView opcUaXMLDataView;

    //静态变量用于存储服务启动是加载的Xml文件节点
    public static List<OpcUaItemDataView> opcUaItemDataViewList;

    //加载XMl文档到内存中
    @PostConstruct
    public void loadXMLDocument() {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(opcUaConfig.getXmlAddress());
        opcUaXMLDataView = dom4jUtil.getDataView(in);
        log.info("----------------------获取xml节点成功----------------------");
    }
}
