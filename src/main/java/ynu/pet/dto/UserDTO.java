package ynu.pet.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String username;
    private String phone;
    private String avatar;
    private String token;
    private String password;
}