package com.stu.helloserver.model.dto;

import lombok.Data;

@Data
public class ChatRequestDTO {
    private String sessionId;   // 会话编号，用于标识同一连续对话
    private String message;     // 当前用户输入
}