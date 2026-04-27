package ynu.pet.controller;

import ynu.pet.dto.PetDTO;
import ynu.pet.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ynu.pet.service.PetService;

import java.util.List;

@Tag(name = "宠物档案", description = "管理我的宠物，用于行为监测")
@RestController
@RequestMapping("/api/pet")
public class PetController {

    @Autowired
    private PetService petService;

    @Operation(summary = "创建宠物档案", description = "添加新宠物到系统")
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/create")
    public Result<Void> createPet(
            @RequestBody PetDTO dto,
            @Parameter(hidden = true) @RequestAttribute Long userId) {
        return petService.createPet(dto, userId);
    }

    @Operation(summary = "获取宠物详情")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/{id}")
    public Result<PetDTO> getPet(
            @Parameter(description = "宠物ID") @PathVariable Long id) {
        return petService.getPetById(id);
    }

    @Operation(summary = "我的宠物列表")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/list")
    public Result<List<PetDTO>> getMyPets(
            @Parameter(hidden = true) @RequestAttribute Long userId) {
        return petService.getUserPets(userId);
    }

    @Operation(summary = "芯片号查询", description = "通过芯片编号查询宠物")
    @GetMapping("/chip/{chipNumber}")
    public Result<PetDTO> getPetByChip(
            @Parameter(description = "芯片编号") @PathVariable String chipNumber) {
        return petService.getPetByChipNumber(chipNumber);
    }

    @Operation(summary = "更新宠物信息")
    @SecurityRequirement(name = "BearerAuth")
    @PutMapping("/{id}")
    public Result<Void> updatePet(
            @Parameter(description = "宠物ID") @PathVariable Long id,
            @RequestBody PetDTO dto) {
        dto.setId(id);
        return petService.updatePet(dto);
    }

    @Operation(summary = "删除宠物档案")
    @SecurityRequirement(name = "BearerAuth")
    @DeleteMapping("/{id}")
    public Result<Void> deletePet(
            @Parameter(description = "宠物ID") @PathVariable Long id,
            @Parameter(hidden = true) @RequestAttribute Long userId) {
        return petService.deletePet(id, userId);
    }
}