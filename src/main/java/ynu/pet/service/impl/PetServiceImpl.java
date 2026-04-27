package ynu.pet.service.impl;

import ynu.pet.dto.PetDTO;
import ynu.pet.dto.Result;
import ynu.pet.entity.Image;
import ynu.pet.entity.Pet;
import ynu.pet.entity.User;
import ynu.pet.mapper.ImageMapper;
import ynu.pet.mapper.PetMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ynu.pet.service.PetService;

import java.util.ArrayList;
import java.util.List;

@Service
public class PetServiceImpl implements PetService {

    @Autowired
    private PetMapper petMapper;

    @Autowired
    private ImageMapper imageMapper;

    @Override
    @Transactional
    public Result<Void> createPet(PetDTO dto, Long ownerId) {
        Pet pet = new Pet();
        BeanUtils.copyProperties(dto, pet);

        User owner = new User();
        owner.setId(ownerId);
        pet.setOwner(owner);

        petMapper.insert(pet);

        // 保存图片
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            for (int i = 0; i < dto.getImages().size(); i++) {
                Image image = new Image();
                image.setImageUrl(dto.getImages().get(i));
                image.setSortOrder(i);

                Pet petRef = new Pet();
                petRef.setId(pet.getId());
                image.setPet(petRef);
                image.setImageType(Image.ImageType.PET);
                imageMapper.insert(image);
            }
        }

        return Result.success();
    }

    @Override
    public Result<PetDTO> getPetById(Long id) {
        Pet pet = petMapper.selectById(id);
        if (pet == null) {
            return Result.error("宠物不存在");
        }
        return Result.success(convertToDTO(pet));
    }

    @Override
    public Result<List<PetDTO>> getUserPets(Long ownerId) {
        List<Pet> pets = petMapper.selectByOwnerId(ownerId);
        List<PetDTO> dtoList = new ArrayList<>();

        for (Pet pet : pets) {
            dtoList.add(convertToDTO(pet));
        }

        return Result.success(dtoList);
    }

    @Override
    public Result<PetDTO> getPetByChipNumber(String chipNumber) {
        Pet pet = petMapper.selectByChipNumber(chipNumber);
        if (pet == null) {
            return Result.error("未找到该芯片编号的宠物");
        }
        return Result.success(convertToDTO(pet));
    }

    @Override
    @Transactional
    public Result<Void> updatePet(PetDTO dto) {
        Pet pet = new Pet();
        BeanUtils.copyProperties(dto, pet);
        petMapper.update(pet);
        return Result.success();
    }

    @Override
    @Transactional
    public Result<Void> deletePet(Long id, Long ownerId) {
        Pet pet = petMapper.selectById(id);
        if (pet == null || !pet.getOwner().getId().equals(ownerId)) {
            return Result.error("无权限删除");
        }

        petMapper.deleteById(id);
        return Result.success();
    }

    private PetDTO convertToDTO(Pet pet) {
        PetDTO dto = new PetDTO();
        BeanUtils.copyProperties(pet, dto);

        // 设置宠物类型描述
        if (pet.getPetType() != null) {
            dto.setPetTypeDesc(pet.getPetType().getDescription());
        }

        // 计算年龄
        dto.setAge(pet.getAge());

        // 查询图片
        List<Image> images = imageMapper.selectByPetId(pet.getId());
        List<String> imageUrls = new ArrayList<>();
        for (Image img : images) {
            imageUrls.add(img.getImageUrl());
        }
        dto.setImages(imageUrls);

        return dto;
    }
}