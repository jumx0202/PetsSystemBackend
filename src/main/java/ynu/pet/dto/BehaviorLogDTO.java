package ynu.pet.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BehaviorLogDTO {
    private Long id;
    private Long petId;
    private String petName;
    private LocalDateTime recordTime;
    private String behaviorType;
    private String emotionType;
    private BigDecimal confidence;
    private Integer healthScore;
    private Boolean abnormalAlert;
    private String alertMessage;
    private List<String> images;
}