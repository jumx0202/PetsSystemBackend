package ynu.pet.service;

import ynu.pet.dto.AiChatRequestDTO;
import ynu.pet.dto.Result;

import java.util.Map;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AiChatService {
    Result<Map<String, String>> chat(AiChatRequestDTO request);
    SseEmitter streamChat(AiChatRequestDTO request);
}
