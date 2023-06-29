package com.taiji.opcuabackend.config;

import com.taiji.opcuabackend.lisenter.RedisKeyExpiredListener;
import com.taiji.opcuabackend.lisenter.RedisUpdateAndAddListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Slf4j
@Configuration
@EnableCaching
public class RedisCacheConfig {

    @Bean
    RedisMessageListenerContainer container(RedisConnectionFactory connectionFactory,
                                            MessageListenerAdapter listenerAdapter1, MessageListenerAdapter listenerAdapter2) {

        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        // 可以添加多个 messageListener，配置不同的交换机
        container.addMessageListener(listenerAdapter1, new PatternTopic("__keyevent@*__:set"));
        container.addMessageListener(listenerAdapter2, new PatternTopic("__keyevent@*__:expired"));


        return container;
    }

    @Bean
    MessageListenerAdapter listenerAdapter1(RedisUpdateAndAddListener receiver) {
        log.info("------------------------Redis消息适配器1启动成功------------------------");
        return new MessageListenerAdapter(receiver, "onMessage");
    }

    @Bean
    MessageListenerAdapter listenerAdapter2(RedisKeyExpiredListener receiver) {
        log.info("------------------------Redis消息适配器2启动成功------------------------");
        return new MessageListenerAdapter(receiver, "onMessage");
    }

}
