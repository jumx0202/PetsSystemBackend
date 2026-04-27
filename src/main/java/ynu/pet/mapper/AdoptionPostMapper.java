package ynu.pet.mapper;

import ynu.pet.entity.AdoptionPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface AdoptionPostMapper {
    int countAll();
    int countByContactPhone(@Param("contactPhone") String contactPhone);
    int updatePublisherByContactPhone(@Param("contactPhone") String contactPhone, @Param("publisherId") Long publisherId);

    // 插入帖子
    int insert(AdoptionPost post);

    // 根据ID查询（包含发布者信息）
    AdoptionPost selectById(Long id);

    // 条件查询列表
    List<AdoptionPost> selectByCondition(@Param("city") String city,
                                         @Param("gender") String gender,
                                         @Param("breed") String breed,
                                         @Param("status") Integer status);

    // 查询用户的所有帖子
    List<AdoptionPost> selectByUserId(Long userId);

    // 更新帖子
    int update(AdoptionPost post);

    // 更新状态
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    // 删除帖子
    int deleteById(Long id);

    // 增加浏览量
    int incrementViewCount(Long id);
}