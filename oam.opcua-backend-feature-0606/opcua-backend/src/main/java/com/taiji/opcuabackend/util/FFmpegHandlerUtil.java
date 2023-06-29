package com.taiji.opcuabackend.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class FFmpegHandlerUtil extends Thread{

    //定义一个Map来存放treadId和process得对应关系
    private Map<String, Process> processMap = new HashMap<>();
    //用来开启一个线程执行command命令，并标记该线程id
    public void startThread(String command, String threadId) {
        //创建一个线程
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                //执行命令
                try {
                    log.info("执行命令：" + command);
                    Process process = Runtime.getRuntime().exec(command);
                    //将线程id和process存入map中
                    processMap.put(threadId, process);
                    BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                    String msg = "";
                    while ((msg = br.readLine()) != null){
                        log.info(msg);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        //设置线程id
        thread.setName(threadId);
        //开启线程
        thread.start();
    }

    //用来根据线程id停止线程
    public void stopThread(String threadId) {
        //根据线程id获取线程
        Thread thread = getThread(threadId);
        //判断线程是否为空
        if (thread != null) {
            //停止线程
            thread.stop();
            //根据线程id获取process
            Process process = processMap.get(threadId);
            //判断process是否为空
            if (process != null) {
                //销毁process
                process.destroy();
                //从map中移除线程id和process
                processMap.remove(threadId);
            }
        }
    }

    //用来根据线程id获取线程
    private Thread getThread(String threadId) {
        //获取当前所有线程
        ThreadGroup threadGroup = Thread.currentThread().getThreadGroup();
        //定义一个线程数组
        Thread[] threads = new Thread[threadGroup.activeCount()];
        //将当前线程的所有线程复制到线程数组中
        threadGroup.enumerate(threads);
        //遍历线程数组
        for (Thread thread : threads) {
            //判断线程id是否和传递的线程id相同
            if (thread.getName().equals(threadId)) {
                //返回线程
                return thread;
            }
        }
        //如果没有找到线程，返回null
        return null;
    }

    //写一个方法，根据线程id判断线程是否存活
    public boolean isAlive(String threadId) {
        //根据线程id获取线程
        Thread thread = getThread(threadId);
        //判断线程是否为空
        if (thread != null) {
            //返回线程是否存活
            return thread.isAlive();
        }
        //如果没有找到线程，返回false
        return false;
    }

}
