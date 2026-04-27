package ynu.pet.service;

import ynu.pet.dto.LostPostDTO;
import ynu.pet.dto.PageResult;
import ynu.pet.dto.Result;
import java.util.List;

public interface LostPostService {
    Result<Void> createPost(LostPostDTO dto, Long publisherId);
    Result<LostPostDTO> getPostById(Long id);
    Result<PageResult<LostPostDTO>> listPosts(String city, String gender, String breed,
                                              Integer pageNum, Integer pageSize);
    Result<List<LostPostDTO>> getUserPosts(Long userId);
    Result<Void> updatePost(LostPostDTO dto);
    Result<Void> deletePost(Long id, Long userId);
    Result<Void> markAsFound(Long id, Long userId);
}