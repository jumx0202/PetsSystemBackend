package ynu.pet.dto;

import lombok.Data;

import java.util.List;

@Data
public class PetFaceMatchSaveDTO {
    private String queryImageUrl;
    private List<PetFaceMatchResultDTO> matches;
}
