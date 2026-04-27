package ynu.pet.controller;

import org.springframework.http.MediaType;
import ynu.pet.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Tag(name = "文件上传", description = "图片上传接口")
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Value("${upload.path}")
    private String uploadPath;

    @Value("${upload.url-prefix}")
    private String urlPrefix;

    @Operation(summary = "上传单张图片", description = "支持jpg、png格式，最大10MB")
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> uploadImage(
            @Parameter(description = "图片文件", required = true, content = @Content(mediaType = "image/*"))
            @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return Result.error("文件为空");
        }

        // 校验文件类型
        String originalName = file.getOriginalFilename();
        String suffix = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        if (!suffix.matches("\\.(jpg|jpeg|png)$")) {
            return Result.error("仅支持jpg、png格式");
        }

        String newName = UUID.randomUUID().toString() + suffix;
        File dest = new File(uploadPath + "/images/" + newName);

        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }

        try {
            file.transferTo(dest);
            return Result.success(urlPrefix + "/images/" + newName);
        } catch (IOException e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }

    @Operation(summary = "上传多张图片")
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<List<String>> uploadImages(
            @Parameter(description = "多个图片文件") @RequestParam("files") List<MultipartFile> files) {

        List<String> urls = new ArrayList<>();

        for (MultipartFile file : files) {
            Result<String> result = uploadImage(file);
            if (result.getCode() == 200) {
                urls.add(result.getData());
            }
        }

        return Result.success(urls);
    }
}