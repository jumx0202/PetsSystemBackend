package ynu.pet.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.math.BigDecimal;


@Data
@Entity
@Table(name = "behavior_models")
public class BehaviorModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    @Column(name = "model_version", nullable = false, length = 20)
    private String modelVersion;

    @Column(name = "model_type", nullable = false)
    private Integer modelType;

    @Column(name = "model_file_path", nullable = false, length = 500)
    private String modelFilePath;

    @Column(name = "accuracy", precision = 5, scale = 2)
    private BigDecimal accuracy;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ========== 无参构造 ==========
    public BehaviorModel() {}
}
