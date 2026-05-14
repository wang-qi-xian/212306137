package com.stu.helloserver.service.impl;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.stu.helloserver.model.dto.ChatRequestDTO;
import com.stu.helloserver.model.vo.ChatResponseVO;
import com.stu.helloserver.service.ChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final StringRedisTemplate stringRedisTemplate;

    // 保留最近对话轮数（3轮表示最近3次用户与助手的对话，即6条消息）
    private static final int MAX_HISTORY_ROUNDS = 3;

    public ChatServiceImpl(ChatClient.Builder chatClientBuilder,
                           StringRedisTemplate stringRedisTemplate) {
        this.chatClient = chatClientBuilder
                .defaultSystem("你是一名专业、友好、简洁的中文智能助手，请结合历史对话上下文，准确回答用户的问题。")
                .defaultOptions(DashScopeChatOptions.builder()
                        .withTopP(0.7)
                        .build())
                .build();
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public ChatResponseVO chat(ChatRequestDTO requestDTO) {
        String sessionId = requestDTO.getSessionId();
        String currentMessage = requestDTO.getMessage();

        // 校验 sessionId 不能为空（可根据需要决定是否抛异常）
        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new IllegalArgumentException("sessionId 不能为空");
        }

        String redisKey = "chat:session:" + sessionId;

        // 1. 读取历史消息（List 中每个元素是一轮对话的字符串）
        List<String> historyList = stringRedisTemplate.opsForList().range(redisKey, 0, -1);
        StringBuilder historyBuilder = new StringBuilder();
        if (historyList != null && !historyList.isEmpty()) {
            for (String record : historyList) {
                historyBuilder.append(record).append("\n");
            }
        }
        String historyText = historyBuilder.toString();

        // 2. 拼接上下文（将历史消息与当前问题组合）
        String finalPrompt;
        if (historyText.isEmpty()) {
            finalPrompt = currentMessage;
        } else {
            finalPrompt = "以下是历史对话：\n" + historyText + "\n当前用户问题：" + currentMessage;
        }

        // 3. 调用大模型
        String answer = chatClient.prompt(finalPrompt).call().content();

        // 4. 保存本轮对话记录
        String newRecord = "用户：" + currentMessage + "\n助手：" + answer;
        stringRedisTemplate.opsForList().rightPush(redisKey, newRecord);

        // 5. 只保留最近 MAX_HISTORY_ROUNDS 轮对话
        Long size = stringRedisTemplate.opsForList().size(redisKey);
        if (size != null && size > MAX_HISTORY_ROUNDS) {
            // 保留最后 MAX_HISTORY_ROUNDS 个元素
            stringRedisTemplate.opsForList().trim(redisKey, size - MAX_HISTORY_ROUNDS, -1);
        }

        return new ChatResponseVO(currentMessage, answer);
    }
}