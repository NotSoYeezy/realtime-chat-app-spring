package com.chat.realtimechat.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class UploadUrlConfig {
    @Getter
    private static String uploadUrl;

    @Value("${url.backend_upload}")
    public void setUploadUrl(String url) {
        UploadUrlConfig.uploadUrl = url;
    }

}