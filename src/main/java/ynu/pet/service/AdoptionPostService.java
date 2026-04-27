package ynu.pet.service;

import ynu.pet.dto.AdoptionPostDTO;
import ynu.pet.dto.PageResult;
import ynu.pet.dto.Result;
import java.util.List;

public interface AdoptionPostService {
    Result<Void> createPost(AdoptionPostDTO dto, Long publisherId);
    Result<AdoptionPostDTO> getPostById(Long id);
    Result<PageResult<AdoptionPostDTO>> listPosts(String city, String gender, String breed,
                                                  Integer pageNum, Integer pageSize);
    Result<List<AdoptionPostDTO>> getUserPosts(Long userId);
    Result<Void> updatePost(AdoptionPostDTO dto);
    Result<Void> deletePost(Long id, Long userId);
    Result<Void> adoptPost(Long id, Long adopterId);
}