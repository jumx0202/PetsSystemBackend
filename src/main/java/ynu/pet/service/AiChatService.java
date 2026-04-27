package ynu.pet.service;

import ynu.pet.dto.AiChatRequestDTO;
import ynu.pet.dto.Result;

import java.util.Map;

public interface AiChatService {
    Result<Map<String, String>> chat(AiChatRequestDTO request);
}
