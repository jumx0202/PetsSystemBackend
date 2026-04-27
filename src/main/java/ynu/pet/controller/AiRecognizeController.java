package ynu.pet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import ynu.pet.dto.Result;

import java.io.IOException;
import java.util.Map;

@Tag(name = "AI识别", description = "宠物品种识别（转发至 Python 推理服务）")
@RestController
@RequestMapping("/api/ai")
public class AiRecognizeController {

    private static final String AI_SERVICE_URL = "http://localhost:8000/api/recognize";

    private final RestTemplate restTemplate;

    public AiRecognizeController() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5_000);   // 连接超时 5s
        factory.setReadTimeout(60_000);     // 读取超时 60s（模型推理最多约 30s）
        this.restTemplate = new RestTemplate(factory);
    }

    @Operation(summary = "品种识别", description = "上传宠物图片，返回品种名称、置信度及 Top-5 列表")
    @PostMapping(value = "/recognize", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Object> recognize(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请上传图片文件");
        }

        try {
            byte[] bytes = file.getBytes();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(bytes) {
                @Override
                public String getFilename() {
                    String name = file.getOriginalFilename();
                    return name != null ? name : "image.jpg";
                }
            });

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    AI_SERVICE_URL,
                    new HttpEntity<>(body, headers),
                    Map.class
            );

            // 解包 Python 服务的外层 {code, message, data}，只返回 data 部分
            Map<?, ?> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("data")) {
                return Result.success(responseBody.get("data"));
            }
            return Result.success(responseBody);

        } catch (ResourceAccessException e) {
            return Result.error(503, "AI 推理服务未启动，请先运行 inference_server.py");
        } catch (IOException e) {
            return Result.error("图片读取失败：" + e.getMessage());
        } catch (Exception e) {
            return Result.error("识别失败：" + e.getMessage());
        }
    }
}
