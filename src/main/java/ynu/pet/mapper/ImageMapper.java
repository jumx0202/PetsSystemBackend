package ynu.pet.mapper;

import ynu.pet.entity.Image;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface ImageMapper {

    int insert(Image image);

    // 批量插入
    int batchInsert(@Param("images") List<Image> images);

    // 根据ID查询
    Image selectById(Long id);

    // 查询领养帖子的图片
    List<Image> selectByAdoptionPostId(Long postId);

    // 查询寻宠帖子的图片
    List<Image> selectByLostPostId(Long postId);

    // 查询宠物档案的图片
    List<Image> selectByPetId(Long petId);

    // 查询行为监测的图片
    List<Image> selectByBehaviorLogId(Long logId);

    // 删除图片
    int deleteById(Long id);

    // 批量删除
    int batchDelete(@Param("ids") List<Long> ids);
}