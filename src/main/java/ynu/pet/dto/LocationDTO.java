package ynu.pet.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 位置数据传输对象，用于 WebSocket 广播和 REST 接口返回
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationDTO {

    /** 宠物ID */
    private Long petId;

    /** 宠物名称（方便前端显示） */
    private String petName;

    /** 纬度 */
    private Double latitude;

    /** 经度 */
    private Double longitude;

    /** 速度 km/h */
    private Float speed;

    /** 数据来源 */
    private String source;

    /** 上报时间 */
    private LocalDateTime recordedAt;
}
