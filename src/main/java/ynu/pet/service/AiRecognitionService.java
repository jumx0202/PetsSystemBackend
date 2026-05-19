package ynu.pet.service;

import org.springframework.web.multipart.MultipartFile;
import ynu.pet.dto.AiRecognitionResultDTO;
import ynu.pet.dto.Result;

public interface AiRecognitionService {
    Result<AiRecognitionResultDTO> recognize(MultipartFile file);
}
