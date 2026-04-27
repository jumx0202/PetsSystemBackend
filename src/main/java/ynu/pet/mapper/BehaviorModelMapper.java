package ynu.pet.mapper;

import ynu.pet.entity.BehaviorModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface BehaviorModelMapper {

    int insert(BehaviorModel model);

    // 根据ID查询
    BehaviorModel selectById(Long id);

    // 查询所有启用的模型
    List<BehaviorModel> selectActiveModels();

    // 根据类型查询
    List<BehaviorModel> selectByType(Integer type);

    // 查询所有模型
    List<BehaviorModel> selectAll();

    // 更新模型
    int update(BehaviorModel model);

    // 启用/禁用模型
    int updateStatus(@Param("id") Long id, @Param("isActive") Boolean isActive);

    // 删除模型
    int deleteById(Long id);
}