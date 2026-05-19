package ynu.pet.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
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
import ynu.pet.dto.PetFaceMatchResultDTO;
import ynu.pet.dto.PetFaceMatchSaveDTO;
import ynu.pet.dto.PetFaceStatusDTO;
import ynu.pet.dto.PetFaceVerifyResultDTO;
import ynu.pet.dto.Result;
import ynu.pet.entity.Image;
import ynu.pet.entity.Pet;
import ynu.pet.entity.PetFaceEmbedding;
import ynu.pet.entity.PetMatchRecord;
import ynu.pet.mapper.ImageMapper;
import ynu.pet.mapper.PetFaceEmbeddingMapper;
import ynu.pet.mapper.PetMatchRecordMapper;
import ynu.pet.mapper.PetMapper;
import ynu.pet.service.PetFaceService;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class PetFaceServiceImpl implements PetFaceService {

    @Value("${ai.petface-embed-url:http://localhost:8000/api/petface/embed}")
    private String petfaceEmbedUrl;

    @Value("${ai.petface-verify-url:http://localhost:8000/api/petface/verify}")
    private String petfaceVerifyUrl;

    private final PetFaceEmbeddingMapper petFaceEmbeddingMapper;
    private final PetMatchRecordMapper petMatchRecordMapper;
    private final ImageMapper imageMapper;
    private final PetMapper petMapper;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PetFaceServiceImpl(PetFaceEmbeddingMapper petFaceEmbeddingMapper,
                              PetMatchRecordMapper petMatchRecordMapper,
                              ImageMapper imageMapper,
                              PetMapper petMapper) {
        this.petFaceEmbeddingMapper = petFaceEmbeddingMapper;
        this.petMatchRecordMapper = petMatchRecordMapper;
        this.imageMapper = imageMapper;
        this.petMapper = petMapper;
    }

    @Override
    public Result<PetFaceStatusDTO> rebuildPetFace(Long petId) {
        Pet pet = petMapper.selectById(petId);
        if (pet == null) {
            return Result.error(404, "宠物不存在");
        }
        List<String> imageUrls = collectPetImageUrls(pet);
        if (imageUrls.isEmpty()) {
            return Result.error(400, "该宠物暂无图片，无法建立个体识别特征");
        }

        try {
            List<List<Double>> embeddings = new ArrayList<>();
            List<String> usedUrls = new ArrayList<>();
            String modelVersion = "PetFace-ID-2.0";
            int embeddingDim = 512;
            for (String imageUrl : imageUrls) {
                try {
                    byte[] imageBytes = restTemplate.getForObject(URI.create(imageUrl), byte[].class);
                    if (imageBytes == null || imageBytes.length == 0) {
                        continue;
                    }
                    JsonNode data = postImageBytes(petfaceEmbedUrl, "file", imageBytes, "pet.jpg").path("data");
                    List<Double> oneEmbedding = objectMapper.convertValue(data.path("embedding"), new TypeReference<List<Double>>() {});
                    if (oneEmbedding != null && !oneEmbedding.isEmpty()) {
                        embeddings.add(oneEmbedding);
                        usedUrls.add(imageUrl);
                        modelVersion = text(data, "model_version", modelVersion);
                        embeddingDim = data.path("embedding_dim").asInt(oneEmbedding.size());
                    }
                } catch (Exception ignored) {
                    // 单张图读取失败不影响建档，所有图片都失败时再返回错误。
                }
            }
            if (embeddings.isEmpty()) {
                return Result.error(400, "宠物图片读取失败，无法建立个体识别特征");
            }
            List<Double> embedding = averageEmbedding(embeddings);
            List<EmbeddingItem> embeddingItems = new ArrayList<>();
            for (int i = 0; i < embeddings.size(); i++) {
                embeddingItems.add(new EmbeddingItem(usedUrls.get(i), embeddings.get(i)));
            }

            PetFaceEmbedding entity = new PetFaceEmbedding();
            entity.setPetId(petId);
            entity.setImageUrl(usedUrls.get(0));
            entity.setImageUrls(objectMapper.writeValueAsString(usedUrls));
            entity.setImageCount(usedUrls.size());
            entity.setModelVersion(modelVersion);
            entity.setEmbeddingDim(embeddingDim);
            entity.setEmbeddingJson(objectMapper.writeValueAsString(embedding));
            entity.setEmbeddingItemsJson(objectMapper.writeValueAsString(embeddingItems));
            petFaceEmbeddingMapper.upsert(entity);
            return Result.success(statusFrom(entity, true));
        } catch (Exception e) {
            return Result.error(500, "建立个体识别特征失败：" + e.getMessage());
        }
    }

    @Override
    public Result<PetFaceStatusDTO> getPetFaceStatus(Long petId) {
        Pet pet = petMapper.selectById(petId);
        if (pet == null) {
            return Result.error(404, "宠物不存在");
        }
        PetFaceEmbedding embedding = petFaceEmbeddingMapper.selectByPetId(petId);
        if (embedding == null) {
            PetFaceStatusDTO status = new PetFaceStatusDTO();
            status.setPetId(petId);
            status.setReady(false);
            return Result.success(status);
        }
        return Result.success(statusFrom(embedding, true));
    }

    @Override
    public Result<PetFaceVerifyResultDTO> verify(MultipartFile file1, MultipartFile file2) {
        if (file1 == null || file1.isEmpty() || file2 == null || file2.isEmpty()) {
            return Result.error(400, "请上传两张宠物图片");
        }
        try {
            JsonNode data = postTwoImages(petfaceVerifyUrl, file1, file2).path("data");
            PetFaceVerifyResultDTO dto = new PetFaceVerifyResultDTO();
            dto.setSamePet(data.path("same_pet").asBoolean());
            dto.setSimilarity(data.path("similarity").asDouble());
            dto.setThreshold(data.path("threshold").asDouble());
            dto.setConfidenceLevel(text(data, "confidence_level", null));
            dto.setModelVersion(text(data, "model_version", "PetFace-ID-2.0"));
            return Result.success(dto);
        } catch (Exception e) {
            return Result.error(500, "同宠验证失败：" + e.getMessage());
        }
    }

    @Override
    public Result<List<PetFaceMatchResultDTO>> search(MultipartFile file, Integer topK) {
        if (file == null || file.isEmpty()) {
            return Result.error(400, "请上传宠物图片");
        }
        int limit = topK == null || topK <= 0 ? 5 : Math.min(topK, 20);
        try {
            JsonNode data = postMultipartImage(petfaceEmbedUrl, "file", file).path("data");
            List<Double> query = objectMapper.convertValue(data.path("embedding"), new TypeReference<List<Double>>() {});
            String modelVersion = text(data, "model_version", "PetFace-ID-2.0");

            List<PetFaceMatchResultDTO> matches = new ArrayList<>();
            for (PetFaceEmbedding stored : petFaceEmbeddingMapper.selectAll()) {
                double similarity = bestSimilarity(query, stored);
                Pet pet = petMapper.selectById(stored.getPetId());
                if (pet == null) {
                    continue;
                }
                matches.add(toMatchResult(pet, round(similarity), confidenceLevel(similarity), modelVersion));
            }

            matches.sort(Comparator.comparing(PetFaceMatchResultDTO::getSimilarity).reversed());
            if (matches.size() > limit) {
                matches = new ArrayList<>(matches.subList(0, limit));
            }
            return Result.success(matches);
        } catch (Exception e) {
            return Result.error(500, "相似宠物检索失败：" + e.getMessage());
        }
    }

    @Override
    public Result<Void> saveLostPostMatches(Long lostPostId, PetFaceMatchSaveDTO dto) {
        if (lostPostId == null) {
            return Result.error(400, "寻宠帖子ID不能为空");
        }
        petMatchRecordMapper.deleteByLostPostId(lostPostId);
        if (dto == null || dto.getMatches() == null || dto.getMatches().isEmpty()) {
            return Result.success();
        }
        for (PetFaceMatchResultDTO match : dto.getMatches()) {
            if (match.getPetId() == null || match.getSimilarity() == null) {
                continue;
            }
            PetMatchRecord record = new PetMatchRecord();
            record.setLostPostId(lostPostId);
            record.setQueryImageUrl(dto.getQueryImageUrl());
            record.setMatchedPetId(match.getPetId());
            record.setSimilarity(match.getSimilarity());
            record.setConfidenceLevel(match.getConfidenceLevel());
            record.setModelVersion(match.getModelVersion() == null ? "PetFace-ID-2.0" : match.getModelVersion());
            petMatchRecordMapper.insert(record);
        }
        return Result.success();
    }

    @Override
    public Result<List<PetFaceMatchResultDTO>> getLostPostMatches(Long lostPostId) {
        List<PetFaceMatchResultDTO> matches = new ArrayList<>();
        for (PetMatchRecord record : petMatchRecordMapper.selectByLostPostId(lostPostId)) {
            Pet pet = record.getMatchedPetId() == null ? null : petMapper.selectById(record.getMatchedPetId());
            if (pet == null) {
                continue;
            }
            matches.add(toMatchResult(pet, record.getSimilarity(), record.getConfidenceLevel(), record.getModelVersion()));
        }
        return Result.success(matches);
    }

    private JsonNode postMultipartImage(String url, String fieldName, MultipartFile file) throws Exception {
        return postImageBytes(url, fieldName, file.getBytes(), file.getOriginalFilename());
    }

    private JsonNode postImageBytes(String url, String fieldName, byte[] bytes, String filename) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add(fieldName, new NamedByteArrayResource(bytes, filename == null ? "pet.jpg" : filename));
        ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
        return objectMapper.readTree(response.getBody());
    }

    private JsonNode postTwoImages(String url, MultipartFile file1, MultipartFile file2) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file1", new NamedByteArrayResource(file1.getBytes(), file1.getOriginalFilename()));
        body.add("file2", new NamedByteArrayResource(file2.getBytes(), file2.getOriginalFilename()));
        ResponseEntity<String> response = restTemplate.postForEntity(url, new HttpEntity<>(body, headers), String.class);
        return objectMapper.readTree(response.getBody());
    }

    private PetFaceStatusDTO statusFrom(PetFaceEmbedding embedding, boolean ready) {
        PetFaceStatusDTO status = new PetFaceStatusDTO();
        status.setPetId(embedding.getPetId());
        status.setReady(ready);
        status.setModelVersion(embedding.getModelVersion());
        status.setEmbeddingDim(embedding.getEmbeddingDim());
        status.setImageUrl(embedding.getImageUrl());
        status.setImageCount(embedding.getImageCount());
        return status;
    }

    private List<String> collectPetImageUrls(Pet pet) {
        Set<String> urls = new LinkedHashSet<>();
        String avatar = firstNonBlank(pet.getAvatar());
        if (avatar != null) {
            urls.add(avatar);
        }
        for (Image image : imageMapper.selectByPetId(pet.getId())) {
            String imageUrl = firstNonBlank(image.getImageUrl());
            if (imageUrl != null) {
                urls.add(imageUrl);
            }
        }
        return new ArrayList<>(urls);
    }

    private String firstNonBlank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String text(JsonNode node, String field, String defaultValue) {
        JsonNode value = node.path(field);
        return value.isMissingNode() || value.isNull() ? defaultValue : value.asText();
    }

    private double cosine(List<Double> a, List<Double> b) {
        if (a == null || b == null || a.isEmpty() || a.size() != b.size()) {
            return 0.0;
        }
        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < a.size(); i++) {
            double av = a.get(i);
            double bv = b.get(i);
            dot += av * bv;
            normA += av * av;
            normB += bv * bv;
        }
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }

    private double bestSimilarity(List<Double> query, PetFaceEmbedding stored) throws Exception {
        double best = 0.0;
        String itemsJson = stored.getEmbeddingItemsJson();
        if (itemsJson != null && !itemsJson.isBlank()) {
            List<EmbeddingItem> items = objectMapper.readValue(itemsJson, new TypeReference<List<EmbeddingItem>>() {});
            for (EmbeddingItem item : items) {
                best = Math.max(best, cosine(query, item.getEmbedding()));
            }
            if (best > 0.0) {
                return best;
            }
        }
        List<Double> target = objectMapper.readValue(stored.getEmbeddingJson(), new TypeReference<List<Double>>() {});
        return cosine(query, target);
    }

    private List<Double> averageEmbedding(List<List<Double>> embeddings) {
        int dim = embeddings.get(0).size();
        double[] values = new double[dim];
        int count = 0;
        for (List<Double> embedding : embeddings) {
            if (embedding == null || embedding.size() != dim) {
                continue;
            }
            count++;
            for (int i = 0; i < dim; i++) {
                values[i] += embedding.get(i);
            }
        }
        double norm = 0.0;
        for (int i = 0; i < dim; i++) {
            values[i] /= Math.max(count, 1);
            norm += values[i] * values[i];
        }
        norm = Math.sqrt(norm);
        List<Double> averaged = new ArrayList<>(dim);
        for (double value : values) {
            averaged.add(norm == 0.0 ? 0.0 : value / norm);
        }
        return averaged;
    }

    private String confidenceLevel(double similarity) {
        if (similarity >= 0.75) {
            return "high";
        }
        if (similarity >= 0.60) {
            return "medium";
        }
        return "low";
    }

    private double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

    private PetFaceMatchResultDTO toMatchResult(Pet pet, Double similarity, String confidenceLevel, String modelVersion) {
        PetFaceMatchResultDTO dto = new PetFaceMatchResultDTO();
        dto.setPetId(pet.getId());
        dto.setPetName(pet.getPetName());
        dto.setPetType(pet.getPetType() == null ? null : pet.getPetType().ordinal());
        dto.setPetTypeDesc(pet.getPetType() == null ? null : pet.getPetType().getDescription());
        dto.setBreed(pet.getBreed());
        dto.setGender(pet.getGender());
        dto.setAvatar(pet.getAvatar());
        dto.setOwnerName(pet.getOwner() == null ? null : pet.getOwner().getUsername());
        dto.setOwnerPhone(pet.getOwner() == null ? null : pet.getOwner().getPhone());
        dto.setSimilarity(similarity);
        dto.setConfidenceLevel(confidenceLevel);
        dto.setModelVersion(modelVersion);
        return dto;
    }

    private static class NamedByteArrayResource extends ByteArrayResource {
        private final String filename;

        NamedByteArrayResource(byte[] byteArray, String filename) {
            super(byteArray);
            this.filename = filename == null ? "pet.jpg" : filename;
        }

        @Override
        public String getFilename() {
            return filename;
        }
    }

    private static class EmbeddingItem {
        private String imageUrl;
        private List<Double> embedding;

        EmbeddingItem() {
        }

        EmbeddingItem(String imageUrl, List<Double> embedding) {
            this.imageUrl = imageUrl;
            this.embedding = embedding;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public List<Double> getEmbedding() {
            return embedding;
        }

        public void setEmbedding(List<Double> embedding) {
            this.embedding = embedding;
        }
    }
}
