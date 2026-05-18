package ynu.pet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ynu.pet.dto.AiRecognitionResultDTO;
import ynu.pet.dto.Result;
import ynu.pet.service.AiRecognitionService;

@Tag(name = "AI识别", description = "宠物品种识别，供系统各模块复用")
@RestController
@RequestMapping("/api/ai")
public class AiRecognizeController {

    @Autowired
    private AiRecognitionService aiRecognitionService;

    @Operation(summary = "品种识别", description = "上传宠物图片，返回宠物种类、品种和候选结果")
    @PostMapping(value = "/recognize", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<AiRecognitionResultDTO> recognize(@RequestParam("file") MultipartFile file) {
        return aiRecognitionService.recognize(file);
    }
}
