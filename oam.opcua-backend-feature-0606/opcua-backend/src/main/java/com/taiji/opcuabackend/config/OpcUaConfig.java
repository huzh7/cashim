package com.taiji.opcuabackend.config;

import lombok.Data;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

/**
 * @author sunzb
 */
@Component
@ConfigurationProperties(prefix = "opcua")
@Data
public class OpcUaConfig {

    private String endpointUrl;

    private String xmlAddress;

    private int tokenTimeout;

    public Predicate<EndpointDescription> endpointFilter() {
        return e -> true;
    }

}
