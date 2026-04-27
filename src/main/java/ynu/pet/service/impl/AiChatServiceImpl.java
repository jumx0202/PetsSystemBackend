package ynu.pet.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ynu.pet.dto.AiChatRequestDTO;
import ynu.pet.dto.Result;
import ynu.pet.service.AiChatService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AiChatServiceImpl implements AiChatService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${ai.base-url:https://api.deepseek.com/v1}")
    private String baseUrl;

    @Value("${ai.path:/chat/completions}")
    private String path;

    @Value("${ai.api-key:}")
    private String apiKey;

    @Value("${ai.model:deepseek-chat}")
    private String model;

    @Value("${ai.timeout-ms:30000}")
    private Integer timeoutMs;

    @Override
    public Result<Map<String, String>> chat(AiChatRequestDTO request) {
        try {
            String normalizedApiKey = normalizeApiKey(apiKey);
            if (normalizedApiKey.isBlank()) {
                return Result.error(500, "AI API Key 未配置，请在后端 application.yml 配置 ai.api-key");
            }

            List<AiChatRequestDTO.Message> inputMessages =
                    request.getMessages() == null ? List.of() : request.getMessages();
            if (inputMessages.isEmpty()) {
                return Result.error(400, "messages 不能为空");
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("model", model);
            payload.put("messages", inputMessages);
            payload.put("temperature", 0.7);

            String endpoint = joinUrl(baseUrl, path);
            String requestBody = objectMapper.writeValueAsString(payload);

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofMillis(Math.max(timeoutMs, 1000)))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + normalizedApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 401) {
                log.error("AI 鉴权失败，status={}, body={}", response.statusCode(), response.body());
                return Result.error(401, "AI 鉴权失败：请检查 ai.api-key 是否正确，且不要重复包含 Bearer 前缀");
            }
            if (response.statusCode() == 402) {
                log.error("AI 账户余额不足，status={}, body={}", response.statusCode(), response.body());
                return Result.error(402, "AI 账户余额不足：请充值后重试，或切换到其他可用模型平台");
            }

            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.error("AI 接口调用失败，status={}, body={}", response.statusCode(), response.body());
                return Result.error(500, "AI 服务调用失败: HTTP " + response.statusCode());
            }

            JsonNode root = objectMapper.readTree(response.body());
            String content = root.path("choices")
                    .path(0)
                    .path("message")
                    .path("content")
                    .asText("");
            if (content == null || content.isBlank()) {
                content = root.path("data").path("content").asText("");
            }
            if (content == null || content.isBlank()) {
                return Result.error(500, "AI 返回内容为空");
            }

            Map<String, String> data = new HashMap<>();
            data.put("content", content);
            return Result.success(data);
        } catch (Exception e) {
            log.error("AI 对话异常", e);
            return Result.error(500, "AI 服务异常，请稍后重试");
        }
    }

    private String joinUrl(String rawBase, String rawPath) {
        String safeBase = rawBase == null ? "" : rawBase.trim();
        String safePath = rawPath == null ? "" : rawPath.trim();
        if (safeBase.endsWith("/")) {
            safeBase = safeBase.substring(0, safeBase.length() - 1);
        }
        if (!safePath.startsWith("/")) {
            safePath = "/" + safePath;
        }
        return safeBase + safePath;
    }

    private String normalizeApiKey(String rawApiKey) {
        if (rawApiKey == null) return "";
        String key = rawApiKey.trim();
        if (key.regionMatches(true, 0, "Bearer ", 0, 7)) {
            key = key.substring(7).trim();
        }
        return key;
    }
}
