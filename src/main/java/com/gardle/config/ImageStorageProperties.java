package com.gardle.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "file")
public class ImageStorageProperties {

    public static final String THUMBNAIL_FOLDER_NAME = "thumbnails";

    private String imageDir;

    public String getImageDir() {
        return imageDir;
    }

    public void setImageDir(String imageDir) {
        this.imageDir = imageDir;
    }
}
