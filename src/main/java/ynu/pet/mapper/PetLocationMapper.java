package ynu.pet.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;
import ynu.pet.entity.PetLocation;

import java.util.List;

@Mapper
@Repository
public interface PetLocationMapper {

    /** 插入一条位置记录 */
    int insert(PetLocation location);

    /** 查询某只宠物的最新一条位置记录 */
    PetLocation selectLatestByPetId(Long petId);

    /** 查询某只宠物过去 N 小时内的历史轨迹（按时间升序） */
    List<PetLocation> selectHistoryByPetId(@Param("petId") Long petId, @Param("hours") int hours);

    /** 查询所有宠物的最新位置（用于 Mock 任务获取需要更新的宠物列表） */
    List<Long> selectAllPetIds();
}
