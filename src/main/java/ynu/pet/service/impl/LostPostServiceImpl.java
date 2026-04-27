package ynu.pet.service.impl;

import ynu.pet.dto.LostPostDTO;
import ynu.pet.dto.PageResult;
import ynu.pet.dto.Result;
import ynu.pet.dto.UserDTO;
import ynu.pet.entity.Image;
import ynu.pet.entity.LostPost;
import ynu.pet.entity.User;
import ynu.pet.mapper.ImageMapper;
import ynu.pet.mapper.LostPostMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ynu.pet.service.LostPostService;

import java.util.ArrayList;
import java.util.List;

@Service
public class LostPostServiceImpl implements LostPostService {

    @Autowired
    private LostPostMapper postMapper;

    @Autowired
    private ImageMapper imageMapper;

    @Override
    @Transactional
    public Result<Void> createPost(LostPostDTO dto, Long publisherId) {
        LostPost post = new LostPost();
        BeanUtils.copyProperties(dto, post);

        User publisher = new User();
        publisher.setId(publisherId);
        post.setPublisher(publisher);

        post.setStatus(LostPost.LostStatus.SEARCHING);
        postMapper.insert(post);

        // 保存图片
        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            for (int i = 0; i < dto.getImages().size(); i++) {
                String rawUrl = dto.getImages().get(i);
                if (rawUrl == null || rawUrl.isBlank()) {
                    continue;
                }
                String safeUrl = rawUrl.startsWith("data:")
                        ? "https://images.unsplash.com/photo-1543466835-00a7907e9de1?w=400&h=400&fit=crop"
                        : rawUrl;
                Image image = new Image();
                image.setImageUrl(safeUrl);
                image.setSortOrder(i);
                image.setLostPostId(post.getId());
                image.setImageType(Image.ImageType.LOST);
                imageMapper.insert(image);
            }
        }

        return Result.success();
    }

    @Override
    public Result<LostPostDTO> getPostById(Long id) {
        LostPost post = postMapper.selectById(id);
        if (post == null) {
            return Result.error("帖子不存在");
        }
        return Result.success(convertToDTO(post));
    }

    @Override
    public Result<PageResult<LostPostDTO>> listPosts(String city, String gender,
                                                     String breed, Integer pageNum, Integer pageSize) {
        List<LostPost> posts = postMapper.selectByCondition(
                city, gender, breed, LostPost.LostStatus.SEARCHING.getValue());
        List<LostPostDTO> dtoList = new ArrayList<>();

        for (LostPost post : posts) {
            dtoList.add(convertToDTO(post));
        }

        PageResult<LostPostDTO> pageResult = new PageResult<>((long) dtoList.size(), dtoList, pageNum, pageSize);
        return Result.success(pageResult);
    }

    @Override
    public Result<List<LostPostDTO>> getUserPosts(Long userId) {
        List<LostPost> posts = postMapper.selectByUserId(userId);
        List<LostPostDTO> dtoList = new ArrayList<>();

        for (LostPost post : posts) {
            dtoList.add(convertToDTO(post));
        }

        return Result.success(dtoList);
    }

    @Override
    @Transactional
    public Result<Void> updatePost(LostPostDTO dto) {
        LostPost post = new LostPost();
        BeanUtils.copyProperties(dto, post);
        postMapper.update(post);
        return Result.success();
    }

    @Override
    @Transactional
    public Result<Void> deletePost(Long id, Long userId) {
        LostPost post = postMapper.selectById(id);
        if (post == null || !post.getPublisher().getId().equals(userId)) {
            return Result.error("无权限删除");
        }

        postMapper.deleteById(id);
        return Result.success();
    }

    @Override
    @Transactional
    public Result<Void> markAsFound(Long id, Long userId) {
        LostPost post = postMapper.selectById(id);
        if (post == null || !post.getPublisher().getId().equals(userId)) {
            return Result.error("无权限操作");
        }

        postMapper.markAsFound(id);
        return Result.success();
    }

    private LostPostDTO convertToDTO(LostPost post) {
        LostPostDTO dto = new LostPostDTO();
        BeanUtils.copyProperties(post, dto);

        if (post.getPublisher() != null) {
            UserDTO userDTO = new UserDTO();
            BeanUtils.copyProperties(post.getPublisher(), userDTO);
            dto.setPublisher(userDTO);
        }

        // 查询图片
        List<Image> images = imageMapper.selectByLostPostId(post.getId());
        List<String> imageUrls = new ArrayList<>();
        for (Image img : images) {
            imageUrls.add(img.getImageUrl());
        }
        dto.setImages(imageUrls);

        return dto;
    }
}