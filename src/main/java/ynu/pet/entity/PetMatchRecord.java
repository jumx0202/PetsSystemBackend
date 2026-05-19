package ynu.pet.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PetMatchRecord {
    private Long id;
    private Long lostPostId;
    private String queryImageUrl;
    private Long matchedPetId;
    private Double similarity;
    private String confidenceLevel;
    private String modelVersion;
    private LocalDateTime createdAt;
}
