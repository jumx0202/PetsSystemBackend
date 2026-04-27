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

    @Override
    public org.springframework.web.servlet.mvc.method.annotation.SseEmitter streamChat(AiChatRequestDTO request) {
        org.springframework.web.servlet.mvc.method.annotation.SseEmitter emitter = new org.springframework.web.servlet.mvc.method.annotation.SseEmitter(120000L); // 2 分钟超时

        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                String normalizedApiKey = normalizeApiKey(apiKey);
                if (normalizedApiKey.isBlank()) {
                    emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event().name("error").data("AI API Key 未配置"));
                    emitter.complete();
                    return;
                }

                List<AiChatRequestDTO.Message> inputMessages =
                        request.getMessages() == null ? new java.util.ArrayList<>() : new java.util.ArrayList<>(request.getMessages());

                if (inputMessages.isEmpty()) {
                    emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event().name("error").data("messages 不能为空"));
                    emitter.complete();
                    return;
                }

                // 注入 System Prompt 设置 AI 身份
                AiChatRequestDTO.Message systemPrompt = new AiChatRequestDTO.Message();
                systemPrompt.setRole("system");
                systemPrompt.setContent("你是一个专业的宠物健康与养护专家 AI 助手。你拥有丰富的宠物饲养、疾病预防、行为训练知识。你的名字叫'宠物小助手'。请用温暖、专业的语气回答用户关于宠物的问题。如果用户提问与宠物完全无关，请礼貌地拒绝回答并引导回宠物话题。");
                inputMessages.add(0, systemPrompt);

                Map<String, Object> payload = new HashMap<>();
                payload.put("model", model);
                payload.put("messages", inputMessages);
                payload.put("temperature", 0.7);
                payload.put("stream", true); // 开启流式输出

                String endpoint = joinUrl(baseUrl, path);
                String requestBody = objectMapper.writeValueAsString(payload);

                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .timeout(Duration.ofMillis(Math.max(timeoutMs, 60000))) // 流式请求给更长超时
                        .header("Content-Type", "application/json")
                        .header("Authorization", "Bearer " + normalizedApiKey)
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
                client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofLines())
                        .thenAccept(response -> {
                            if (response.statusCode() >= 400) {
                                try {
                                    emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event().name("error").data("AI 服务调用失败: HTTP " + response.statusCode()));
                                    emitter.complete();
                                } catch (Exception ex) {
                                    emitter.completeWithError(ex);
                                }
                                return;
                            }
                            try (java.util.stream.Stream<String> lines = response.body()) {
                                lines.forEach(line -> {
                                    if (line.startsWith("data: ") && !line.equals("data: [DONE]")) {
                                        try {
                                            String dataStr = line.substring(6);
                                            JsonNode root = objectMapper.readTree(dataStr);
                                            JsonNode deltaNode = root.path("choices").path(0).path("delta").path("content");
                                            if (!deltaNode.isMissingNode() && !deltaNode.isNull()) {
                                                String content = deltaNode.asText();
                                                System.out.println(System.currentTimeMillis() + " Sending chunk: " + content);
                                                // 将片段封装为 JSON 发送给前端
                                                emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event().data(Map.of("delta", content)));
                                            }
                                        } catch (Exception e) {
                                            log.warn("解析 SSE stream line 失败: {}", line, e);
                                        }
                                    }
                                });
                                emitter.complete();
                            } catch (Exception e) {
                                emitter.completeWithError(e);
                            }
                        })
                        .exceptionally(ex -> {
                            try {
                                emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event().name("error").data("网络请求异常"));
                                emitter.completeWithError(ex);
                            } catch (Exception e) {}
                            return null;
                        });

            } catch (Exception e) {
                log.error("AI 流式对话发送异常", e);
                try {
                    emitter.send(org.springframework.web.servlet.mvc.method.annotation.SseEmitter.event().name("error").data("服务器内部异常"));
                    emitter.completeWithError(e);
                } catch (Exception ex) {}
            }
        });

        return emitter;
    }
}
