package com.temple;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // Serve CSS
        registry.addResourceHandler("/css/**")
            .addResourceLocations("classpath:/static/css/");

        // Serve JS
        registry.addResourceHandler("/js/**")
            .addResourceLocations("classpath:/static/js/");

        // Serve QR codes from both locations
        registry.addResourceHandler("/qrcodes/**")
            .addResourceLocations(
                "classpath:/static/qrcodes/",
                "file:src/main/resources/static/qrcodes/",
                "file:target/classes/static/qrcodes/"
            );

        // ✅ THIS IS THE FIX — Serve audio files from both locations
        registry.addResourceHandler("/audio/**")
            .addResourceLocations(
                "classpath:/static/audio/",
                "file:src/main/resources/static/audio/",
                "file:target/classes/static/audio/"
            );
    }
}