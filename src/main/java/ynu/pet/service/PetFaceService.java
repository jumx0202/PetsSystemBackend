package ynu.pet.service;

import org.springframework.web.multipart.MultipartFile;
import ynu.pet.dto.PetFaceMatchResultDTO;
import ynu.pet.dto.PetFaceMatchSaveDTO;
import ynu.pet.dto.PetFaceStatusDTO;
import ynu.pet.dto.PetFaceVerifyResultDTO;
import ynu.pet.dto.Result;

import java.util.List;

public interface PetFaceService {
    Result<PetFaceStatusDTO> rebuildPetFace(Long petId);

    Result<PetFaceStatusDTO> getPetFaceStatus(Long petId);

    Result<PetFaceVerifyResultDTO> verify(MultipartFile file1, MultipartFile file2);

    Result<List<PetFaceMatchResultDTO>> search(MultipartFile file, Integer topK);

    Result<Void> saveLostPostMatches(Long lostPostId, PetFaceMatchSaveDTO dto);

    Result<List<PetFaceMatchResultDTO>> getLostPostMatches(Long lostPostId);
}
