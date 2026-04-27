package ynu.pet.service.impl;

import ynu.pet.dto.AdoptionPostDTO;
import ynu.pet.dto.PageResult;
import ynu.pet.dto.Result;
import ynu.pet.dto.UserDTO;
import ynu.pet.entity.AdoptionPost;
import ynu.pet.entity.Image;
import ynu.pet.entity.User;
import ynu.pet.mapper.AdoptionPostMapper;
import ynu.pet.mapper.ImageMapper;
import ynu.pet.mapper.UserMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ynu.pet.service.AdoptionPostService;

import java.util.ArrayList;
import java.util.List;

@Service
public class AdoptionPostServiceImpl implements AdoptionPostService {

    @Autowired
    private AdoptionPostMapper postMapper;

    @Autowired
    private ImageMapper imageMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public Result<Void> createPost(AdoptionPostDTO dto, Long publisherId) {
        // 创建帖子
        AdoptionPost post = new AdoptionPost();
        BeanUtils.copyProperties(dto, post);

        User publisher = new User();
        publisher.setId(publisherId);
        post.setPublisher(publisher);

        post.setStatus(AdoptionPost.PostStatus.SEARCHING);
        postMapper.insert(post);

        // 保存图片
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            saveImages(post.getId(), dto.getImages());
        }

        return Result.success();
    }

    @Override
    public Result<AdoptionPostDTO> getPostById(Long id) {
        // 增加浏览量
        postMapper.incrementViewCount(id);

        AdoptionPost post = postMapper.selectById(id);
        if (post == null) {
            return Result.error("帖子不存在");
        }

        AdoptionPostDTO dto = convertToDTO(post);
        return Result.success(dto);
    }

    @Override
    public Result<PageResult<AdoptionPostDTO>> listPosts(String city, String gender,
                                                         String breed, Integer pageNum, Integer pageSize) {
        List<AdoptionPost> posts = postMapper.selectByCondition(
                city, gender, breed, AdoptionPost.PostStatus.SEARCHING.getValue());
        List<AdoptionPostDTO> dtoList = new ArrayList<>();

        for (AdoptionPost post : posts) {
            dtoList.add(convertToDTO(post));
        }

        // 简化：实际需要count查询
        PageResult<AdoptionPostDTO> pageResult = new PageResult<>((long) dtoList.size(), dtoList, pageNum, pageSize);
        return Result.success(pageResult);
    }

    @Override
    public Result<List<AdoptionPostDTO>> getUserPosts(Long userId) {
        List<AdoptionPost> posts = postMapper.selectByUserId(userId);
        List<AdoptionPostDTO> dtoList = new ArrayList<>();

        for (AdoptionPost post : posts) {
            dtoList.add(convertToDTO(post));
        }

        return Result.success(dtoList);
    }

    @Override
    @Transactional
    public Result<Void> updatePost(AdoptionPostDTO dto) {
        AdoptionPost post = new AdoptionPost();
        BeanUtils.copyProperties(dto, post);
        postMapper.update(post);
        return Result.success();
    }

    @Override
    @Transactional
    public Result<Void> deletePost(Long id, Long userId) {
        // 验证权限
        AdoptionPost post = postMapper.selectById(id);
        if (post == null || !post.getPublisher().getId().equals(userId)) {
            return Result.error("无权限删除");
        }

        postMapper.deleteById(id);
        return Result.success();
    }

    @Override
    @Transactional
    public Result<Void> adoptPost(Long id, Long adopterId) {
        postMapper.updateStatus(id, AdoptionPost.PostStatus.FOUND.getValue());
        return Result.success();
    }

    // 辅助方法：保存图片
    private void saveImages(Long postId, List<String> imageUrls) {
        for (int i = 0; i < imageUrls.size(); i++) {
            String rawUrl = imageUrls.get(i);
            if (rawUrl == null || rawUrl.isBlank()) {
                continue;
            }
            // 兜底：避免前端直接传 base64 导致字段超长
            String safeUrl = rawUrl.startsWith("data:")
                    ? "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=400&h=400&fit=crop"
                    : rawUrl;
            Image image = new Image();
            image.setImageUrl(safeUrl);
            image.setSortOrder(i);
            image.setAdoptionPostId(postId);
            image.setImageType(Image.ImageType.ADOPTION);
            imageMapper.insert(image);
        }
    }

    // 辅助方法：转换为DTO
    private AdoptionPostDTO convertToDTO(AdoptionPost post) {
        AdoptionPostDTO dto = new AdoptionPostDTO();
        BeanUtils.copyProperties(post, dto);

        // 设置发布者信息
        if (post.getPublisher() != null) {
            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(post.getPublisher(), userDTO);
            dto.setPublisher(userDTO);
        }

        // 查询图片
        List<Image> images = imageMapper.selectByAdoptionPostId(post.getId());
        List<String> imageUrls = new ArrayList<>();
        for (Image img : images) {
            imageUrls.add(img.getImageUrl());
        }
        dto.setImages(imageUrls);

        return dto;
    }
}