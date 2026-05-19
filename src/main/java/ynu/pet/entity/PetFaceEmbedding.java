package ynu.pet.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PetFaceEmbedding {
    private Long id;
    private Long petId;
    private String imageUrl;
    private String imageUrls;
    private Integer imageCount;
    private String modelVersion;
    private Integer embeddingDim;
    private String embeddingJson;
    private String embeddingItemsJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
