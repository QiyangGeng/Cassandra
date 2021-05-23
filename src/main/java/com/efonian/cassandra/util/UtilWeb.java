package com.efonian.cassandra.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Nullable;
import java.net.MalformedURLException;
import java.net.URL;


public final class UtilWeb {
    private static final Logger logger = LoggerFactory.getLogger(UtilWeb.class);
    
    @Nullable
    public static URL getURL(String url) {
        try {
            return new URL(url);
        } catch(MalformedURLException e) {
            logger.warn("MalformedURLException: " + e.getMessage());
            return null;
        }
    }
    
    @Nullable
    public static <T> T processUrl(String url, Class<T> type) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<T> call = restTemplate.getForEntity(url, type);
        switch(call.getStatusCodeValue()) {
            case 200:
                return call.getBody();
                
            case 400:
                logger.warn("Bad request (400):" + url);
                return null;
            
            case 404:
                logger.warn("Not Found (404): " + url);
                return null;
            
            default:
                logger.warn("Unknown response (" + call.getStatusCodeValue() + "): " + url);
                return call.getBody();
        }
    }
}
