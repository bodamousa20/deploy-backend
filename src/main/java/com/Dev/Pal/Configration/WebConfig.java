package com.Dev.Pal.Configration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
@Value("${frontend.url}")
private String frontendUrl ;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Allow requests from your specific domain
        registry.addMapping("/**")
                .allowedOrigins(frontendUrl)  // Allowed origin
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Allowed methods
                .allowedHeaders("*")  // Allow all headers
                .allowCredentials(true) // Allow credentials (cookies, authorization headers)
                .maxAge(3600);  // Cache pre-flight response for 1 hour
    }
}
