package com.stu.helloserver.controller;

import com.stu.helloserver.common.Result;
import com.stu.helloserver.model.dto.ChatRequestDTO;
import com.stu.helloserver.model.vo.ChatResponseVO;
import com.stu.helloserver.service.ChatService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public Result<ChatResponseVO> chat(@RequestBody ChatRequestDTO requestDTO) {
        // 可以增加 sessionId 为空的默认处理，这里直接交给 Service 校验
        ChatResponseVO responseVO = chatService.chat(requestDTO);
        return Result.success(responseVO);
    }
}