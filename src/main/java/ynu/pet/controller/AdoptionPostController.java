package ynu.pet.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import ynu.pet.dto.AdoptionPostDTO;
import ynu.pet.dto.PageResult;
import ynu.pet.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ynu.pet.service.AdoptionPostService;

import java.util.List;

@Tag(name = "领养管理", description = "发布领养、浏览领养帖子、申请领养")
@RestController
@RequestMapping("/api/adoption")
public class AdoptionPostController {

    @Autowired
    @Qualifier("adoptionPostServiceImpl")
    private AdoptionPostService postService;

    @Operation(summary = "发布领养帖子", description = "发布宠物领养信息，需要登录")
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/publish")
    public Result<Void> createPost(
            @RequestBody AdoptionPostDTO dto,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        return postService.createPost(dto, userId);
    }

    @Operation(summary = "获取帖子详情", description = "根据ID获取领养帖子详情")
    @GetMapping("/{id}")
    public Result<AdoptionPostDTO> getPost(
            @Parameter(description = "帖子ID") @PathVariable("id") Long id) {
        return postService.getPostById(id);
    }

    @Operation(summary = "条件查询帖子", description = "支持按城市、性别、品种筛选")
    @GetMapping("/list")
    public Result<PageResult<AdoptionPostDTO>> listPosts(
            @Parameter(description = "城市") @RequestParam(required = false) String city,
            @Parameter(description = "性别") @RequestParam(required = false) String gender,
            @Parameter(description = "品种") @RequestParam(required = false) String breed,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量", example = "10") @RequestParam(defaultValue = "10") Integer pageSize) {
        return postService.listPosts(city, gender, breed, pageNum, pageSize);
    }

    @Operation(summary = "我的发布", description = "获取当前用户发布的所有领养帖子")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/my-posts")
    public Result<List<AdoptionPostDTO>> getMyPosts(
            @Parameter(hidden = true)
            @RequestAttribute("userId") Long userId) {
        return postService.getUserPosts(userId);
    }

    @Operation(summary = "更新帖子", description = "修改领养信息")
    @SecurityRequirement(name = "BearerAuth")
    @PutMapping("/{id}")
    public Result<Void> updatePost(
            @Parameter(description = "帖子ID") @PathVariable("id") Long id,
            @RequestBody AdoptionPostDTO dto) {
        dto.setId(id);
        return postService.updatePost(dto);
    }

    @Operation(summary = "删除帖子", description = "删除自己发布的领养帖子")
    @SecurityRequirement(name = "BearerAuth")
    @DeleteMapping("/{id}")
    public Result<Void> deletePost(
            @Parameter(description = "帖子ID") @PathVariable("id") Long id,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        return postService.deletePost(id, userId);
    }

    @Operation(summary = "标记已领养", description = "帖子状态改为已领养")
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/{id}/adopt")
    public Result<Void> adoptPost(
            @Parameter(description = "帖子ID") @PathVariable("id") Long id,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        return postService.adoptPost(id, userId);
    }
}