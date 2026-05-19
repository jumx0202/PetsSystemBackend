package ynu.pet.dto;

import lombok.Data;

@Data
public class PetFaceVerifyResultDTO {
    private Boolean samePet;
    private Double similarity;
    private Double threshold;
    private String confidenceLevel;
    private String modelVersion;
}
