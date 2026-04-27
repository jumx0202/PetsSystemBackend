package ynu.pet.mapper;

import ynu.pet.entity.LostPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface LostPostMapper {
    int countByContactPhone(@Param("contactPhone") String contactPhone);
    int updatePublisherByContactPhone(@Param("contactPhone") String contactPhone, @Param("publisherId") Long publisherId);

    int insert(LostPost post);

    // 根据ID查询
    LostPost selectById(Long id);

    // 条件查询
    List<LostPost> selectByCondition(@Param("city") String city,
                                     @Param("gender") String gender,
                                     @Param("breed") String breed,
                                     @Param("status") Integer status);

    // 查询用户的寻宠帖子
    List<LostPost> selectByUserId(Long userId);

    // 更新状态为已找到
    int markAsFound(Long id);

    // 更新帖子
    int update(LostPost post);

    int deleteById(Long id);
}