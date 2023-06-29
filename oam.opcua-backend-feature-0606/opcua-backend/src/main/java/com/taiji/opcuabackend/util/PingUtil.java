package com.taiji.opcuabackend.util;

import java.io.IOException;
import java.net.InetAddress;

/**
 * @author sunzb
 * @date 2023/6/6 10:28
 */
public class PingUtil {

    //写一个ping的方法，用来检测camera的ip是否可用
    public static boolean isReachable(String ipAddress) throws Exception {
        int timeOut = 3000;  //超时时间
        boolean status = false;     //记录是否成功
        try {
            status = InetAddress.getByName(ipAddress).isReachable(timeOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return status;
    }

}