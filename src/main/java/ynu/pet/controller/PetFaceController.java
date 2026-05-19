package ynu.pet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ynu.pet.dto.PetFaceMatchResultDTO;
import ynu.pet.dto.PetFaceMatchSaveDTO;
import ynu.pet.dto.PetFaceStatusDTO;
import ynu.pet.dto.PetFaceVerifyResultDTO;
import ynu.pet.dto.Result;
import ynu.pet.service.PetFaceService;

import java.util.List;

@Tag(name = "PetFace个体识别", description = "宠物个体特征建立、同宠验证与相似检索")
@RestController
@RequestMapping("/api")
public class PetFaceController {

    private final PetFaceService petFaceService;

    public PetFaceController(PetFaceService petFaceService) {
        this.petFaceService = petFaceService;
    }

    @Operation(summary = "重建宠物个体识别特征")
    @PostMapping("/pet/{petId}/face/rebuild")
    public Result<PetFaceStatusDTO> rebuild(@PathVariable Long petId) {
        return petFaceService.rebuildPetFace(petId);
    }

    @Operation(summary = "查询宠物个体识别特征状态")
    @GetMapping("/pet/{petId}/face/status")
    public Result<PetFaceStatusDTO> status(@PathVariable Long petId) {
        return petFaceService.getPetFaceStatus(petId);
    }

    @Operation(summary = "同宠验证")
    @PostMapping(value = "/ai/petface/verify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<PetFaceVerifyResultDTO> verify(
            @RequestParam("file1") MultipartFile file1,
            @RequestParam("file2") MultipartFile file2) {
        return petFaceService.verify(file1, file2);
    }

    @Operation(summary = "相似宠物检索")
    @PostMapping(value = "/ai/petface/search", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<List<PetFaceMatchResultDTO>> search(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "topK", required = false, defaultValue = "5") Integer topK) {
        return petFaceService.search(file, topK);
    }

    @Operation(summary = "保存寻宠启事的相似匹配结果")
    @PostMapping("/lost/{lostPostId}/petface/matches")
    public Result<Void> saveLostPostMatches(
            @PathVariable Long lostPostId,
            @RequestBody PetFaceMatchSaveDTO dto) {
        return petFaceService.saveLostPostMatches(lostPostId, dto);
    }

    @Operation(summary = "查询寻宠启事的相似匹配结果")
    @GetMapping("/lost/{lostPostId}/petface/matches")
    public Result<List<PetFaceMatchResultDTO>> getLostPostMatches(@PathVariable Long lostPostId) {
        return petFaceService.getLostPostMatches(lostPostId);
    }
}
