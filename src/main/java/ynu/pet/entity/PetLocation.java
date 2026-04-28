package ynu.pet.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PetLocation {

    private Long id;

    /** 关联的宠物ID */
    private Long petId;

    /** 纬度 */
    private Double latitude;

    /** 经度 */
    private Double longitude;

    /** 速度 km/h */
    private Float speed;

    /** 数据来源：MOCK / GPS / MANUAL */
    private String source;

    /** 上报时间 */
    private LocalDateTime recordedAt;

    /** 便捷构造：只传必要字段 */
    public PetLocation(Long petId, double latitude, double longitude, String source) {
        this.petId = petId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.speed = 0f;
        this.source = source;
        this.recordedAt = LocalDateTime.now();
    }
}
