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
 * 宠物 GPS 位置 Mock 定时任务（所有宠物在北京活动）
 *
 * <p>所有宠物的模拟位置都固定在北京（天安门附近）的小范围内连续游走。</p>
 */
@Slf4j
@Component
public class LocationMockTask {

    // 北京基准坐标（天安门广场）
    private static final double BASE_LAT = 39.9042;
    private static final double BASE_LNG = 116.4074;

    // 每个宠物周围游走的最大半径（约 ±150m）
    private static final double MAX_OFFSET = 0.0015;

    // 步进步长（约 ±30m）
    private static final double STEP = 0.0003;

    private final Random random = new Random();

    @Autowired
    private PetLocationService locationService;

    @Autowired
    private PetMapper petMapper;

    /**
     * 存储每只宠物当前的游走位置
     * key: petId, value: [lat, lng]
     */
    private final Map<Long, double[]> currentPositions = new HashMap<>();

    /**
     * 每 5 秒执行一次 Mock 位置上报
     */
    @Scheduled(fixedDelay = 5000)
    public void generateMockLocations() {
        try {
            List<Pet> pets = petMapper.selectAll(0, Integer.MAX_VALUE);
            if (pets.isEmpty()) {
                return;
            }

            for (Pet pet : pets) {
                Long petId = pet.getId();

                // 获取当前游走位置（首次使用基准点 + 小随机偏移）
                double[] pos = currentPositions.get(petId);
                if (pos == null) {
                    double[] newPos = new double[]{
                            BASE_LAT + (random.nextDouble() - 0.5) * MAX_OFFSET * 2,
                            BASE_LNG + (random.nextDouble() - 0.5) * MAX_OFFSET * 2
                    };
                    currentPositions.put(petId, newPos);
                    pos = newPos;
                }

                // 在当前位置基础上做小步随机游走
                pos[0] += (random.nextDouble() - 0.5) * STEP;
                pos[1] += (random.nextDouble() - 0.5) * STEP;

                // 边界反弹：确保宠物不会跑出北京圈定范围
                if (Math.abs(pos[0] - BASE_LAT) > MAX_OFFSET) {
                    pos[0] = BASE_LAT + (random.nextDouble() - 0.5) * MAX_OFFSET;
                }
                if (Math.abs(pos[1] - BASE_LNG) > MAX_OFFSET) {
                    pos[1] = BASE_LNG + (random.nextDouble() - 0.5) * MAX_OFFSET;
                }

                // 保存并通过 WebSocket 广播
                locationService.saveAndBroadcast(petId, pet.getPetName(), pos[0], pos[1], "MOCK");
            }

            log.debug("[Mock] 已为 {} 只宠物更新北京内的位置", pets.size());

        } catch (Exception e) {
            log.warn("[Mock] 位置生成失败: {}", e.getMessage());
        }
    }
}