package com.taiji.opcuabackend;

import com.taiji.opcuabackend.task.OpcUaReadNodeTask;
import com.taiji.opcuabackend.util.RedisUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import springfox.documentation.oas.annotations.EnableOpenApi;

import java.util.Set;

@EnableScheduling
@EnableOpenApi
@SpringBootApplication
@MapperScan("com.taiji.opcuabackend.mapper")
public class OpcUaBackendApplication implements CommandLineRunner {

    @Autowired
    OpcUaReadNodeTask opcUaReadNodeTask;

    public static void main(String[] args) {
        SpringApplication.run(OpcUaBackendApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        opcUaReadNodeTask.subScriptionAllNode();
    }

    @Scheduled(cron ="0 0/5 * * * ?")
    public void cleanRedisClientInfoChannel() {
        opcUaReadNodeTask.cleanRedisClientInfoChannel();
    }
}
