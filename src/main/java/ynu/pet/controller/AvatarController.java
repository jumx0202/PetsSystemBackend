package ynu.pet.controller;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/images")
public class AvatarController {

    @Value("${upload.path}")
    private String uploadPath;

    @GetMapping("/{filename:.+}")
    public void getAvatar(@PathVariable String filename, HttpServletResponse response) throws IOException {
        // 尝试从磁盘读取 {uploadPath}/images/{filename}
        Path diskFile = Paths.get(uploadPath, "images", filename);
        if (Files.exists(diskFile) && Files.isRegularFile(diskFile)) {
            String contentType = Files.probeContentType(diskFile);
            if (contentType == null) contentType = MediaType.IMAGE_PNG_VALUE;
            response.setContentType(contentType);
            Files.copy(diskFile, response.getOutputStream());
            return;
        }

        // 如果磁盘不存在，返回默认图片（classpath:/static/images/default.png）
        ClassPathResource defaultResource = new ClassPathResource("static/images/default.png");
        if (defaultResource.exists()) {
            response.setContentType(MediaType.IMAGE_PNG_VALUE);
            try (InputStream is = defaultResource.getInputStream()) {
                is.transferTo(response.getOutputStream());
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}