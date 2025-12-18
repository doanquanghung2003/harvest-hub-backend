package com.example.harvesthubbackend.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * API Versioning Configuration
 * 
 * Current API version: v1
 * All endpoints are under /api/v1/...
 * 
 * For future versions, create new controllers under /api/v2/...
 */
@Configuration
public class ApiVersionConfig implements WebMvcConfigurer {
    
    // API version constants
    public static final String API_VERSION_V1 = "v1";
    public static final String API_VERSION_V2 = "v2";
    
    // Current API version
    public static final String CURRENT_API_VERSION = API_VERSION_V1;
    
    // Base path for current version
    public static final String API_BASE_PATH = "/api/" + CURRENT_API_VERSION;
}

