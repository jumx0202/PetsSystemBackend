package ynu.pet.dto;

import lombok.Data;

@Data
public class PetFaceMatchResultDTO {
    private Long petId;
    private String petName;
    private Integer petType;
    private String petTypeDesc;
    private String breed;
    private String gender;
    private String avatar;
    private String ownerName;
    private String ownerPhone;
    private Double similarity;
    private String confidenceLevel;
    private String modelVersion;
}
