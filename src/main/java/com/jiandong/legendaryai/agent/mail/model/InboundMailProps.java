package com.jiandong.legendaryai.agent.mail.model;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "inbound-mail")
public record InboundMailProps(String host, Integer port, String username, String password, String protocol) {

}