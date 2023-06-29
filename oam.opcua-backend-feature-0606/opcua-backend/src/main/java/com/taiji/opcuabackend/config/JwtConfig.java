package com.taiji.opcuabackend.config;

import com.taiji.opcuabackend.interceptor.JwtInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author sunzb
 * @date 2023/6/7 18:00
 */
@Configuration
public class JwtConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new JwtInterceptor())
                //添加拦截路径
                .addPathPatterns("/register/**")
                .addPathPatterns("/subscription/**")
                //添加放行路径
                .excludePathPatterns("/register/getToken");
    }
}

