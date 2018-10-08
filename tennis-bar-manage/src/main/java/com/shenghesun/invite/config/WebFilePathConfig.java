package com.shenghesun.invite.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebFilePathConfig extends WebMvcConfigurerAdapter {
	
	@Value("${upload.file.path}")
	private String uploadFilePath;
	@Value("${show.file.path}")
	private String showFilePath;
	

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
    	System.out.println("静态文件映射--------------------" + showFilePath + uploadFilePath);
        registry.addResourceHandler(showFilePath + "**").addResourceLocations("file:" + uploadFilePath);
        super.addResourceHandlers(registry);
    }
}