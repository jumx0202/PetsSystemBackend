package ynu.pet.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;

import java.util.List;
import java.time.LocalDateTime;


@Entity
@Data
@Table(name = "PostAdoption")
public class AdoptionPost {
    @Id
    @GeneratedValue
    private Long id;

    //宠物信息
    //性别：公/母/不详
    private String gender;
    //品种
    private String breed;

    //地点信息
    //城市
    private String city;
    //详细地址
    private String district;

    //联系方式
    //联系人名称
    private String contactName;
    //联系人电话
    private String contactPhone;
    //联系人vx（可填可不填）
    private String contactWechat;

    //描述
    private String description;

    //图片（一对多）
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "postadoption_id")
    private List<Image> images;

    //发贴人
    @ManyToOne
    @JoinColumn(name = "postadoption_publisher_id")
    private User publisher;

    private LocalDateTime createdAt;
    //帖子状态：寻找中/已找到
//    private PostStatus status;

    @Getter
    public enum PostStatus {
        //寻找中
        SEARCHING(1),
        //已找到
        FOUND(2);

        private final int value;
        PostStatus(int value) {
            this.value = value;
        }
    }

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status")
    public PostStatus status = PostStatus.SEARCHING;
}
