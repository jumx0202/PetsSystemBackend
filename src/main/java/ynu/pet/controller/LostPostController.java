package ynu.pet.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import ynu.pet.dto.LostPostDTO;
import ynu.pet.dto.PageResult;
import ynu.pet.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ynu.pet.service.LostPostService;

import java.util.List;

@Tag(name = "寻宠管理", description = "发布寻宠启事、浏览、标记已找到")
@RestController
@RequestMapping("/api/lost")
public class LostPostController {

    @Autowired
    @Qualifier("lostPostServiceImpl")
    private LostPostService postService;

    @Operation(summary = "发布寻宠启事", description = "发布宠物丢失信息")
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/publish")
    public Result<Void> createPost(
            @RequestBody LostPostDTO dto,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        return postService.createPost(dto, userId);
    }

    @Operation(summary = "获取寻宠详情")
    @GetMapping("/{id}")
    public Result<LostPostDTO> getPost(
            @Parameter(description = "帖子ID") @PathVariable("id") Long id) {
        return postService.getPostById(id);
    }

    @Operation(summary = "条件查询寻宠帖子")
    @GetMapping("/list")
    public Result<PageResult<LostPostDTO>> listPosts(
            @Parameter(description = "城市") @RequestParam(required = false) String city,
            @Parameter(description = "性别") @RequestParam(required = false) String gender,
            @Parameter(description = "品种") @RequestParam(required = false) String breed,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        return postService.listPosts(city, gender, breed, pageNum, pageSize);
    }

    @Operation(summary = "我的寻宠发布")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/my-posts")
    public Result<List<LostPostDTO>> getMyPosts(
            @Parameter(hidden = true)
            @RequestAttribute("userId") Long userId) {
        return postService.getUserPosts(userId);
    }

    @Operation(summary = "更新寻宠信息")
    @SecurityRequirement(name = "BearerAuth")
    @PutMapping("/{id}")
    public Result<Void> updatePost(
            @Parameter(description = "帖子ID") @PathVariable("id") Long id,
            @RequestBody LostPostDTO dto) {
        dto.setId(id);
        return postService.updatePost(dto);
    }

    @Operation(summary = "删除寻宠帖子")
    @SecurityRequirement(name = "BearerAuth")
    @DeleteMapping("/{id}")
    public Result<Void> deletePost(
            @Parameter(description = "帖子ID") @PathVariable("id") Long id,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        return postService.deletePost(id, userId);
    }

    @Operation(summary = "标记已找到", description = "宠物找到后标记状态")
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/{id}/found")
    public Result<Void> markAsFound(
            @Parameter(description = "帖子ID") @PathVariable("id") Long id,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId) {
        return postService.markAsFound(id, userId);
    }
}