package com.taiji.opcuabackend.service;

import java.util.List;
import java.util.Set;

public interface PushService {
    /**
     * 推送给指定用户
     */
    void pushMsgToSuscriptionOne(Set<String> userIds, String key, String value);

    /**
     * 推送给所有订阅的用户
     * @param msg
     */
    void pushMsgToSuscriptionUsers(String msg);

}
