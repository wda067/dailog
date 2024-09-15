package com.dailog.api.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                        .exposedHeaders("Set-Cookie");
                        //.allowedOrigins("http://localhost:3000");
            }
        };
    }

    //@Override
    //public void addViewControllers(ViewControllerRegistry registry) {
    //    registry.addViewController("/{spring:[a-zA-Z0-9-]+}")
    //            .setViewName("forward:/index.html");
    //}
}
