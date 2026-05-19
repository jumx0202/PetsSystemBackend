package ynu.pet.dto;

import lombok.Data;

@Data
public class AiRecognitionCandidateDTO {
    private String breed;
    private String breedCn;
    private Double confidence;
}
