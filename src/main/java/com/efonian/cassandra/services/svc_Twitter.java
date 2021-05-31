package com.efonian.cassandra.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "cassandra.twitter.init", havingValue = "true")
public class svc_Twitter {
    private static final Logger logger = LoggerFactory.getLogger(svc_Twitter.class);
    
    @Value("${cassandra.twitter.consumer.key}")
    private String consumerKey;
    @Value("${cassandra.twitter.consumer.secret}")
    private String consumerSecret;
    @Value("${cassandra.twitter.access.token}")
    private String accessToken;
    @Value("${cassandra.twitter.access.secret}")
    private String accessSecret;
    
    
}
