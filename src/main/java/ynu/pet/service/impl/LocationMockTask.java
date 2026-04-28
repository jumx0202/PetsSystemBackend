package ynu.pet.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ynu.pet.entity.Pet;
import ynu.pet.mapper.PetMapper;
import ynu.pet.service.PetLocationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 宠物 GPS 位置 Mock 定时任务
 *
 * <p>在真实产品中，此处应为 GPS 硬件设备定期调用 /api/location/report 接口。
 * 在演示/毕设场景下，我们使用定时任务模拟设备周期性上报行为：
 * 每 5 秒为系统中所有宠物生成一个带随机偏移的坐标，模拟宠物在昆明市中心附近活动。</p>
 */
@Slf4j
@Component
public class LocationMockTask {

    // 基准坐标：云南大学（昆明市中心附近），WGS-84 坐标系
    private static final double BASE_LAT = 25.0692;
    private static final double BASE_LNG = 102.6975;

    // 最大随机游走范围（约 ±150m）
    private static final double MAX_OFFSET = 0.0015;

    private final Random random = new Random();

    @Autowired
    private PetLocationService locationService;

    @Autowired
    private PetMapper petMapper;

    /**
     * 存储每只宠物当前的"漫游位置"，实现连续游走（而不是每次都从基准点跳变）
     * key: petId, value: [lat, lng]
     */
    private final Map<Long, double[]> currentPositions = new HashMap<>();

    /**
     * 每 5 秒执行一次 Mock 位置上报
     * fixedDelay 确保两次任务之间的间隔，而非固定频率
     */
    @Scheduled(fixedDelay = 5000)
    public void generateMockLocations() {
        try {
            // 获取系统中所有宠物ID
            List<Long> petIds = petMapper.selectAll(0, Integer.MAX_VALUE)
                    .stream()
                    .map(Pet::getId)
                    .toList();

            if (petIds.isEmpty()) {
                return;
            }

            for (Long petId : petIds) {
                // 获取该宠物当前位置（首次使用基准点 + 小随机偏移）
                double[] pos = currentPositions.computeIfAbsent(petId, id -> new double[]{
                        BASE_LAT + (random.nextDouble() - 0.5) * MAX_OFFSET * 2,
                        BASE_LNG + (random.nextDouble() - 0.5) * MAX_OFFSET * 2
                });

                // 在当前位置基础上做小步随机游走（±30m）
                double step = 0.0003;
                pos[0] += (random.nextDouble() - 0.5) * step;
                pos[1] += (random.nextDouble() - 0.5) * step;

                // 超出边界时反弹，确保宠物不会跑太远
                if (Math.abs(pos[0] - BASE_LAT) > MAX_OFFSET) pos[0] = BASE_LAT + (random.nextDouble() - 0.5) * MAX_OFFSET;
                if (Math.abs(pos[1] - BASE_LNG) > MAX_OFFSET) pos[1] = BASE_LNG + (random.nextDouble() - 0.5) * MAX_OFFSET;

                // 获取宠物名称（从 Pet 对象）
                Pet pet = petMapper.selectById(petId);
                String petName = pet != null ? pet.getPetName() : "宠物" + petId;

                // 保存并通过 WebSocket 广播
                locationService.saveAndBroadcast(petId, petName, pos[0], pos[1], "MOCK");
            }

            log.debug("[Mock] 已为 {} 只宠物更新位置", petIds.size());

        } catch (Exception e) {
            log.warn("[Mock] 位置生成失败: {}", e.getMessage());
        }
    }
}
