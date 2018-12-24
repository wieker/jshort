package com.example.jshort.configuration;

import com.example.jshort.controllers.RedirectionInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorsConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry interceptorRegistry) {
        interceptorRegistry.addInterceptor(getInterceptor());
    }

    @Bean
    public RedirectionInterceptor getInterceptor() {
        return new RedirectionInterceptor();
    }

}
