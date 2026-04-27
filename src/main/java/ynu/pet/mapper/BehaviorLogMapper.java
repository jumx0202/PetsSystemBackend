package ynu.pet.mapper;

import ynu.pet.entity.BehaviorLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
@Repository
public interface BehaviorLogMapper {

    int insert(BehaviorLog log);

    // 根据ID查询
    BehaviorLog selectById(Long id);

    // 查询宠物的所有监测记录
    List<BehaviorLog> selectByPetId(Long petId);

    // 查询异常记录
    List<BehaviorLog> selectAbnormalByPetId(Long petId);

    // 时间范围查询
    List<BehaviorLog> selectByTimeRange(@Param("petId") Long petId,
                                        @Param("startTime") LocalDateTime startTime,
                                        @Param("endTime") LocalDateTime endTime);

    // 查询最新的记录
    BehaviorLog selectLatestByPetId(Long petId);

    // 统计异常次数
    int countAbnormal(@Param("petId") Long petId, @Param("startTime") LocalDateTime startTime);

    // 删除记录
    int deleteById(Long id);
}