package ynu.pet.service;

import ynu.pet.dto.LocationDTO;
import ynu.pet.dto.Result;

import java.util.List;

public interface PetLocationService {

    /**
     * 保存位置并通过 WebSocket 广播给所有订阅该宠物的客户端
     */
    void saveAndBroadcast(Long petId, String petName, double latitude, double longitude, String source);

    /**
     * 查询宠物最新位置
     */
    Result<LocationDTO> getLatestLocation(Long petId);

    /**
     * 查询宠物历史轨迹（过去N小时）
     */
    Result<List<LocationDTO>> getLocationHistory(Long petId, int hours);

    /**
     * 真实设备上报接口（留接口备用，目前记录并广播）
     */
    Result<Void> reportLocation(Long petId, double latitude, double longitude);
}
