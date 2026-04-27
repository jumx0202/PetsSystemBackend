package ynu.pet.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;


@Entity
@Data
@Table(name = "User")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // 用户名
    private String username;
    // 手机号（唯一，用于登录）
    private String phone;
    // 密码（加密存储）
    private String password;
    // 头像URL
    private String avatar;

    // 关联
    @OneToMany(mappedBy = "publisher")
    private List<AdoptionPost> adoptionPosts;

    @OneToMany(mappedBy = "publisher")
    private List<LostPost> lostPosts;
}
