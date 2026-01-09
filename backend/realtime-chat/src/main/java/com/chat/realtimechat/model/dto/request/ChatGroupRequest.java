package com.chat.realtimechat.model.dto.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Data
public class ChatGroupRequest {
    private String name;
    private Set<Long> memberIds;
}