package ynu.pet.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import ynu.pet.dto.LocationDTO;
import ynu.pet.dto.Result;
import ynu.pet.entity.PetLocation;
import ynu.pet.mapper.PetLocationMapper;
import ynu.pet.mapper.PetMapper;
import ynu.pet.service.PetLocationService;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PetLocationServiceImpl implements PetLocationService {

    @Autowired
    private PetLocationMapper locationMapper;

    @Autowired
    private PetMapper petMapper;

    /**
     * Spring WebSocket 消息模板，用于向前端推送 STOMP 消息
     */
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public void saveAndBroadcast(Long petId, String petName, double latitude, double longitude, String source) {
        // 1. 构造并持久化位置记录
        PetLocation previous = locationMapper.selectLatestByPetId(petId);
        PetLocation record = new PetLocation(petId, latitude, longitude, source);
        record.setSpeed(calculateSpeed(previous, record));
        locationMapper.insert(record);
        log.debug("已保存位置记录: petId={}, lat={}, lng={}", petId, latitude, longitude);

        // 2. 构造广播 DTO
        LocationDTO dto = new LocationDTO(
                petId,
                petName,
                latitude,
                longitude,
                record.getSpeed(),
                source,
                record.getRecordedAt()
        );

        // 3. 向对应 Topic 广播，前端通过 /topic/pet/{petId}/location 订阅
        String destination = "/topic/pet/" + petId + "/location";
        messagingTemplate.convertAndSend(destination, dto);
        log.debug("已广播位置: destination={}", destination);
    }

    private Float calculateSpeed(PetLocation previous, PetLocation current) {
        if (previous == null || previous.getRecordedAt() == null || current.getRecordedAt() == null) {
            return 0f;
        }

        long seconds = Duration.between(previous.getRecordedAt(), current.getRecordedAt()).getSeconds();
        if (seconds <= 0) {
            return 0f;
        }

        double distanceKm = haversineKm(
                previous.getLatitude(), previous.getLongitude(),
                current.getLatitude(), current.getLongitude()
        );
        double speed = distanceKm / (seconds / 3600.0);
        return (float) Math.min(speed, 30.0);
    }

    private double haversineKm(double lat1, double lng1, double lat2, double lng2) {
        final double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }

    @Override
    public Result<LocationDTO> getLatestLocation(Long petId) {
        PetLocation loc = locationMapper.selectLatestByPetId(petId);
        if (loc == null) {
            return Result.error("暂无位置数据");
        }
        // 获取宠物名称
        var pet = petMapper.selectById(petId);
        String petName = pet != null ? pet.getPetName() : "未知";
        LocationDTO dto = new LocationDTO(petId, petName, loc.getLatitude(), loc.getLongitude(),
                loc.getSpeed(), loc.getSource(), loc.getRecordedAt());
        return Result.success(dto);
    }

    @Override
    public Result<List<LocationDTO>> getLocationHistory(Long petId, int hours) {
        if (hours <= 0 || hours > 72) {
            hours = 24; // 默认24小时，最多72小时
        }
        var pet = petMapper.selectById(petId);
        String petName = pet != null ? pet.getPetName() : "未知";
        String finalPetName = petName;
        List<LocationDTO> history = locationMapper.selectHistoryByPetId(petId, hours)
                .stream()
                .map(loc -> new LocationDTO(petId, finalPetName, loc.getLatitude(), loc.getLongitude(),
                        loc.getSpeed(), loc.getSource(), loc.getRecordedAt()))
                .collect(Collectors.toList());
        return Result.success(history);
    }

    @Override
    public Result<Void> reportLocation(Long petId, double latitude, double longitude) {
        // 真实设备上报时使用 GPS 来源标识
        var pet = petMapper.selectById(petId);
        if (pet == null) {
            return Result.error("宠物不存在");
        }
        saveAndBroadcast(petId, pet.getPetName(), latitude, longitude, "GPS");
        return Result.success(null);
    }
}
