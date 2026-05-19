package ynu.pet.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import ynu.pet.entity.PetMatchRecord;

import java.util.List;

@Mapper
@Repository
public interface PetMatchRecordMapper {
    int insert(PetMatchRecord record);

    int deleteByLostPostId(Long lostPostId);

    List<PetMatchRecord> selectByLostPostId(Long lostPostId);
}
