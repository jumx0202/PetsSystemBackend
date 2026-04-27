package ynu.pet.entity;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Data
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //图片URL
    private String imageUrl;
    //排序顺序 ？
    private Integer sortOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id")
    private Pet pet;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "behavior_log_id")
//    private BehaviorLog behaviorLog;

    //外键（二选一）
    private Long adoptionPostId;
    private Long lostPostId;

    // 图片归属类型，对应数据库字段 image_type
    @Enumerated(EnumType.STRING)
    private ImageType imageType;

    public enum ImageType {
        ADOPTION,
        LOST,
        PET
    }
}

