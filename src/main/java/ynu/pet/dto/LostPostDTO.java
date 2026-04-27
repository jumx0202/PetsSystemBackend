package ynu.pet.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class LostPostDTO {
    private Long id;
    private String petName;
    private String gender;
    private String breed;
    private String lostTime;
    private String lostLocation;
    private String city;
    private String district;
    private String contactName;
    private String contactPhone;
    private String contactWechat;
    private String description;
    private String status;
    private LocalDateTime createdAt;
    private UserDTO publisher;
    private List<String> images;
}