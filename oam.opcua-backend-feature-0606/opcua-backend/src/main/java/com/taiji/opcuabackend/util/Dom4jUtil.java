package com.taiji.opcuabackend.util;

import com.taiji.opcuabackend.entity.OpcUaItemDataView;
import com.taiji.opcuabackend.entity.OpcUaNodeDataView;
import com.taiji.opcuabackend.entity.OpcUaXMLDataView;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.*;

/**
 * Springboot整合DOM4J读取XML文件
 */
@Slf4j
@Component
public class Dom4jUtil {

//    private static List<OpcUaItemDataView> opcUaItemDataViews = new ArrayList<OpcUaItemDataView>();

    private static Map<String, OpcUaItemDataView> opcUaItemDataViewMap = new HashMap<>();

    /**
     * 解析XML文件核心方法
     *
     * @param in
     */
    public OpcUaXMLDataView getDataView(InputStream in) {

        // 创建List集合存放相关数据
        List<OpcUaNodeDataView> opcUaNodeDataViewList = new ArrayList<OpcUaNodeDataView>();

        SAXReader reader = new SAXReader();

        // 设置文件编码
        reader.setEncoding("UTF-8");

        // 初始化数据
        OpcUaXMLDataView opcUaXMLDataView = new OpcUaXMLDataView();
        try {
            // 读取文件
            Document document = reader.read(in);
            // 获取根节点标签<dataView></dataView>
            Element dataViewRoot = document.getRootElement();
            // 开始遍历
            Iterator iteratorRoot = dataViewRoot.elementIterator();
            while (iteratorRoot.hasNext()) {
                // 获取相关属性
                Element dataElement = (Element) iteratorRoot.next();
                String nodeName = dataElement.getName();
                //判断节点内容，并给data赋值
                if (dataElement.getName().equals("structure")) {
                    //节点如果未folder，则进行递归操作，遍历所有子节点
                    opcUaXMLDataView.setStructure(getRecursionXmlData(dataElement, null));
                }else if (dataElement.getName().equals("name")){
                    String name = dataElement.getText();
                    // 设置name
                    opcUaXMLDataView.setName(name);
                }else if (dataElement.getName().equals("port")){
                    String port = dataElement.getText();
                    // 设置port
                    opcUaXMLDataView.setPort(port);
                }else if (dataElement.getName().equals("serverCertification")){
                    String serverCertification = dataElement.getText();
                    // 设置serverCertification
                    opcUaXMLDataView.setServerCertification(serverCertification);
                }else if (dataElement.getName().equals("description")){
                    String description = dataElement.getText();
                    // 设置description
                    opcUaXMLDataView.setDescription(description);
                }else if (dataElement.getName().equals("trustedClientCertifications")){
                    //初始化certification列表
                    List<String> certificationList = new ArrayList<String>();
                    Iterator childDataElementLabel = dataElement.elementIterator();
                    while (childDataElementLabel.hasNext()){
                        //获取certification标签内内容
                        Element elementLabel = (Element)childDataElementLabel.next();
                        certificationList.add(elementLabel.getText());
                    }
                    opcUaXMLDataView.setTrustedClientCertifications(certificationList);
                }
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        return opcUaXMLDataView;
    }

    //递归方法，获取xml中所有节点
    private List<OpcUaNodeDataView> getRecursionXmlData(Element dataElement, StringBuffer parentIdentifier){

        List<OpcUaNodeDataView> opcUaNodeDataViewList = new ArrayList<OpcUaNodeDataView>();
        // 获取相关标签
        Iterator dataElementLabel = dataElement.elementIterator();
        // 开始遍历
        while (dataElementLabel.hasNext()) {

            Element elementChild = (Element) dataElementLabel.next();
            String nodeName = elementChild.getName();

            OpcUaNodeDataView opcUaNodeDataView = new OpcUaNodeDataView();
            StringBuffer identifierPrefix = new StringBuffer();
            if ("item".equals(nodeName)) {
                List<Attribute> attributes = elementChild.attributes();
                OpcUaItemDataView opcUaItemDataView = new OpcUaItemDataView();
                for (Attribute attribute : attributes) {
                    if ("name".equals(attribute.getName())) {
                        String name = attribute.getValue();
                        // 设置name
                        opcUaNodeDataView.setName(name);
                        opcUaItemDataView.setName(name);

                        //拼写identifierPrefix前缀
                        if(parentIdentifier != null && parentIdentifier.length() != 0){
                            identifierPrefix.append(parentIdentifier);
                            identifierPrefix.append(".");
                        }else {
                            identifierPrefix = new StringBuffer("");
                        }
                        identifierPrefix.append(name);
                        opcUaItemDataView.setIdentifier(identifierPrefix.toString());
                    }else if ("access".equals(attribute.getName())) {
                        String access = attribute.getValue();
                        // 设置access
                        opcUaNodeDataView.setAccess(access);
                        opcUaItemDataView.setAccess(access);
                    }else if ("datatype".equals(attribute.getName())) {
                        String datatype = attribute.getValue();
                        // 设置datatype
                        opcUaNodeDataView.setDatatype(datatype);
                        opcUaItemDataView.setDatatype(datatype);
                    }else if ("initialValue".equals(attribute.getName())) {
                        String initialValue = attribute.getValue();
                        // 设置initialValue
                        opcUaNodeDataView.setInitialValue(initialValue);
                        opcUaItemDataView.setInitialValue(initialValue);
                    }else if ("description".equals(attribute.getName())) {
                        String description = attribute.getValue();
                        // 设置description
                        opcUaNodeDataView.setDescription(description);
                        opcUaItemDataView.setDescription(description);
                    }
                }
                opcUaItemDataViewMap.put(opcUaItemDataView.getIdentifier(), opcUaItemDataView);
            } else if ("folder".equals(nodeName)) {
                List<Attribute> attributes = elementChild.attributes();
                for (Attribute attribute: attributes) {
                    if ("name".equals(attribute.getName())) {
                        String name = attribute.getValue();
                        // 设置folder节点的name属性
                        opcUaNodeDataView.setName(name);

                        //拼写identifierPrefix前缀
                        if(parentIdentifier != null && parentIdentifier.length() != 0){
                            identifierPrefix.append(parentIdentifier);
                            identifierPrefix.append(".");
                        }else {
                            identifierPrefix = new StringBuffer("");
                        }
                        identifierPrefix.append(name);
                    }
                }
                //若opcua节点为folder，则递归folder内部节点
                List<OpcUaNodeDataView> subOpcUaNodeDataViewList = getRecursionXmlData(elementChild, identifierPrefix);
                opcUaNodeDataView.setFolders(subOpcUaNodeDataViewList);

            }
            opcUaNodeDataViewList.add(opcUaNodeDataView);
        }
        return opcUaNodeDataViewList;
    }


    /**
     * 返回所有Item节点信息
     * @return
     */
    public Map<String, OpcUaItemDataView> getRecursionItemData(){
        return opcUaItemDataViewMap;
    }
}

