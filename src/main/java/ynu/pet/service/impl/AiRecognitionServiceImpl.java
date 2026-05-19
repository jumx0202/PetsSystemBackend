package ynu.pet.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import ynu.pet.dto.AiRecognitionCandidateDTO;
import ynu.pet.dto.AiRecognitionResultDTO;
import ynu.pet.dto.Result;
import ynu.pet.service.AiRecognitionService;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiRecognitionServiceImpl implements AiRecognitionService {

    @Value("${ai.recognize-url:http://localhost:8000/api/recognize}")
    private String recognizeUrl;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Result<AiRecognitionResultDTO> recognize(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error(400, "请上传宠物图片");
        }

        try {
            JsonNode data = postImage(recognizeUrl, "file", file).path("data");
            AiRecognitionResultDTO dto = new AiRecognitionResultDTO();
            dto.setPetType(text(data, "pet_type"));
            dto.setPetTypeCn(text(data, "pet_type_cn"));
            dto.setBreed(text(data, "breed"));
            dto.setBreedCn(text(data, "breed_cn"));
            dto.setConfidence(data.path("confidence").asDouble());

            List<AiRecognitionCandidateDTO> candidates = new ArrayList<>();
            for (JsonNode item : data.path("top5")) {
                AiRecognitionCandidateDTO candidate = new AiRecognitionCandidateDTO();
                candidate.setBreed(text(item, "breed"));
                candidate.setBreedCn(text(item, "breed_cn"));
                candidate.setConfidence(item.path("confidence").asDouble());
                candidates.add(candidate);
            }
            dto.setTop5(candidates);
            return Result.success(dto);
        } catch (Exception e) {
            return Result.error(500, "AI品种识别失败：" + e.getMessage());
        }
    }

    private JsonNode postImage(String url, String fieldName, MultipartFile file) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add(fieldName, toResource(file));

        ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
        return objectMapper.readTree(response.getBody());
    }

    private ByteArrayResource toResource(MultipartFile file) throws Exception {
        return new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename() == null ? "pet.jpg" : file.getOriginalFilename();
            }
        };
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }
}
