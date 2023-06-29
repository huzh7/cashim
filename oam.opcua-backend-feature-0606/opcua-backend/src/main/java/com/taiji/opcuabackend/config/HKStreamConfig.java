package com.taiji.opcuabackend.config;

import lombok.Data;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

/**
 * @author sunzb
 */

@Component
@Data
@Configuration
public class HKStreamConfig {

    @Value("${hk.cameraAccount}")
    private String cameraAccount;

    @Value("${hk.cameraPassword}")
    private String cameraPassword;

    @Value("${hk.registerPort}")
    private String registerPort;

    @Value("${hk.streamPort}")
    private String streamPort;

    @Value("${hk.ngxinRtmpIP}")
    private String ngxinRtmpIP;

    @Value("${hk.ngxinRtmpPort}")
    private String ngxinRtmpPort;

}
