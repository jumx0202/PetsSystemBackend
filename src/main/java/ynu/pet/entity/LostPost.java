package ynu.pet.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;

import java.util.List;
import java.time.LocalDateTime;


@Entity
@Data
@Table(name = "PostLost")
public class LostPost {
    @Id
    @GeneratedValue
    private Long id;

    //宠物基本信息
    //宠物名字
    private String petName;
    //性别
    private String gender;
    //品种
    private String breed;

    //丢失信息
    //丢失时间（字符串，支持灵活格式）
    private String lostTime;
    //城市
    private String city;
    //详细地址
    private String lostLocation;

    //联系方式
    //联系人名称
    private String contactName;
    //联系人电话
    private String contactPhone;
    //联系人vx（可填可不填）
    private String contactWechat;

    //描述
    private String description;

    //图片
    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "postlost_id")
    private List<Image> images;

    //发帖人
    @ManyToOne
    @JoinColumn(name = "postlost_publisher_id")
    private User publisher;

    private LocalDateTime createdAt;
    //帖子状态：寻找中/已找到
    private LostStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lostpet_id")
    private Pet pet;

    @Getter
    public enum LostStatus {
        //寻找中
        SEARCHING(1),
        //已找到
        FOUND(2);

        private final int value;
        LostStatus(int value) {
            this.value = value;
        }
    }

}