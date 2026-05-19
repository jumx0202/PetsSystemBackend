package ynu.pet.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;
import ynu.pet.entity.PetFaceEmbedding;

import java.util.List;

@Mapper
@Repository
public interface PetFaceEmbeddingMapper {
    int upsert(PetFaceEmbedding embedding);

    PetFaceEmbedding selectByPetId(Long petId);

    List<PetFaceEmbedding> selectAll();

    int deleteByPetId(Long petId);
}
