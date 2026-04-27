package ynu.pet.service;

import ynu.pet.dto.Result;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ImageService {
    Result<String> uploadImage(MultipartFile file);
    Result<List<String>> uploadImages(List<MultipartFile> files);
    Result<Void> deleteImage(String imageUrl);
    Result<Void> savePostImages(Long postId, Integer postType, List<String> imageUrls);
}