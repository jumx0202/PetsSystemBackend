package ynu.pet.mapper;

import ynu.pet.entity.Pet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface PetMapper {

    int insert(Pet pet);

    // 根据ID查询
    Pet selectById(Long id);

    // 查询用户的所有宠物
    List<Pet> selectByOwnerId(Long ownerId);

    // 根据芯片号查询
    Pet selectByChipNumber(String chipNumber);

    // 查询所有宠物（分页）
    List<Pet> selectAll(@Param("offset") int offset, @Param("limit") int limit);

    // 更新宠物信息
    int update(Pet pet);

    // 删除宠物
    int deleteById(Long id);
}