package ynu.pet.dto;

import lombok.Data;

@Data
public class PetFaceStatusDTO {
    private Long petId;
    private Boolean ready;
    private String modelVersion;
    private Integer embeddingDim;
    private String imageUrl;
    private Integer imageCount;
}
