package com.bnp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/** Serves uploaded files from the local upload directory at {@code /uploads/**}. */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${bnp.upload.dir:uploads}")
    private String uploadDir;

    @Value("${bnp.upload.public-path:/uploads}")
    private String publicPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = Paths.get(uploadDir).toAbsolutePath().normalize().toUri().toString();
        String pattern = publicPath.replaceAll("/$", "") + "/**";
        registry.addResourceHandler(pattern)
                .addResourceLocations(location)
                .setCachePeriod(3600);
    }
}
