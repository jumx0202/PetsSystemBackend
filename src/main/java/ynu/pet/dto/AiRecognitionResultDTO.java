package ynu.pet.dto;

import lombok.Data;

import java.util.List;

@Data
public class AiRecognitionResultDTO {
    private String petType;
    private String petTypeCn;
    private String breed;
    private String breedCn;
    private Double confidence;
    private List<AiRecognitionCandidateDTO> top5;
}
