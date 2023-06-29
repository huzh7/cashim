package com.taiji.opcuabackend.handler;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.taiji.opcuabackend.config.NettyConfig;
import com.taiji.opcuabackend.constant.ClientInfoConstant;
import com.taiji.opcuabackend.constant.OpcUaConstant;
import com.taiji.opcuabackend.entity.ClientInfo;
import com.taiji.opcuabackend.entity.OpcUaItemDataView;
import com.taiji.opcuabackend.service.ClientInfoService;
import com.taiji.opcuabackend.service.OpcUaSubscriptionService;
import com.taiji.opcuabackend.util.Dom4jUtil;
import com.taiji.opcuabackend.util.RedisUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Component
@ChannelHandler.Sharable
public class OpcUaServerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Autowired
    public ClientInfoService clientInfoService;

    @Autowired
    public RedisUtil redisUtil;

    @Autowired
    public Dom4jUtil dom4jUtil;

    @Autowired
    public OpcUaSubscriptionService opcUaSubscriptionService;

    /**
     * 一旦连接，第一个被执行
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.info("handlerAdded 被调用"+ctx.channel().id().asLongText());
        // 添加到channelGroup 通道组
        NettyConfig.getChannelGroup().add(ctx.channel());
    }

    /**
     * 读取数据
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {

        log.info("服务器收到消息：{}",msg.text());

        // 获取用户ID,关联channel
        JSONObject jsonObject = JSONUtil.parseObj(msg.text());
        String userToken = jsonObject.getStr("token");
        JSONArray nodesArray = jsonObject.getJSONArray("nodes");
        Integer operationType = jsonObject.getInt("operation_type");

        //判断参数是否为空
        if(userToken == null || nodesArray == null || operationType == null){
            Map<String, String> errorResponseMap = new HashMap<>();
            errorResponseMap.put("status", "-2");
            errorResponseMap.put("msg", "参数不能为空");
            ctx.channel().writeAndFlush(new TextWebSocketFrame(new JSONObject(errorResponseMap).toString()));

        }else {
            Map<String, String> responseMap = opcUaSubscriptionService.nodeItemClientSubscription(userToken, nodesArray, operationType);
            ctx.channel().writeAndFlush(new TextWebSocketFrame(new JSONObject(responseMap).toString()));

            // 标记用户与channel的对应关系
            NettyConfig.getUserChannelMap().put(userToken,ctx.channel());
            // 将用户ID作为自定义属性加入到channel中，方便随时channel中获取用户ID
            AttributeKey<String> key = AttributeKey.valueOf("userToken");
            ctx.channel().attr(key).setIfAbsent(userToken);
        }
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        // 删除通道
        NettyConfig.getChannelGroup().remove(ctx.channel());
        removeUserToken(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("异常：{}",cause.getMessage());
        // 删除通道
        NettyConfig.getChannelGroup().remove(ctx.channel());
        removeUserToken(ctx);
        ctx.close();
    }

    /**
     * 删除用户与channel的对应关系
     * @param ctx
     */
    private void removeUserToken(ChannelHandlerContext ctx){
        AttributeKey<String> key = AttributeKey.valueOf("userToken");
        String userToken = ctx.channel().attr(key).get();
        if(userToken != null){
            NettyConfig.getUserChannelMap().remove(userToken);

            try {
                //重置clientInfo通道状态
                ClientInfo ci = new ClientInfo();
                ci.setToken(userToken);
                ci.setIsChannelOn(ClientInfoConstant.CLIENT_INFO_CHANNEL_NO_EXIST);
                clientInfoService.modifyClientInfoChannelStatus(ci);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}