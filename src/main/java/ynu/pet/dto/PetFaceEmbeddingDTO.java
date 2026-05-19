package ynu.pet.dto;

import lombok.Data;

import java.util.List;

@Data
public class PetFaceEmbeddingDTO {
    private String modelVersion;
    private String modelName;
    private Integer embeddingDim;
    private List<Double> embedding;
    private Double norm;
}
