package ynu.pet.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;
import java.time.LocalDateTime;
import java.math.BigDecimal;


@Entity
@Data
@Table(name = "behavior_logs")
public class BehaviorLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id")
    private BehaviorModel model;

    private LocalDateTime recordTime;
    private String behaviorType;
    private String emotionType;
    private BigDecimal confidence;
    private Integer healthScore;
    private Boolean abnormalAlert;

//    @OneToMany(mappedBy = "behaviorLog", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    //使用@JoinColumn代替mappedBy
    @JoinColumn(name = "behavior_log_id")
    private List<Image> images;  // 监测截图/视频帧
}
