package ynu.pet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ynu.pet.dto.AiChatRequestDTO;
import ynu.pet.dto.Result;
import ynu.pet.service.AiChatService;

import java.util.Map;

@Tag(name = "AI 问答", description = "AI 大模型问答接口")
@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    @Autowired
    @Qualifier("aiChatServiceImpl")
    private AiChatService aiChatService;

    @Operation(summary = "AI 对话", description = "传入上下文消息列表，返回模型回复")
    @PostMapping("/chat")
    public Result<Map<String, String>> chat(@RequestBody AiChatRequestDTO request) {
        return aiChatService.chat(request);
    }

    @Operation(summary = "AI 流式对话", description = "返回 SSE 流")
    @PostMapping(value = "/stream-chat", produces = org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE)
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter streamChat(@RequestBody AiChatRequestDTO request) {
        return aiChatService.streamChat(request);
    }
}
