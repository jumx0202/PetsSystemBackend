package ynu.pet.service;

import ynu.pet.dto.BehaviorLogDTO;
import ynu.pet.dto.PageResult;
import ynu.pet.dto.Result;
import java.time.LocalDateTime;
import java.util.List;

public interface BehaviorLogService {
    Result<Void> createLog(BehaviorLogDTO dto);
    Result<BehaviorLogDTO> getLogById(Long id);
    Result<PageResult<BehaviorLogDTO>> listLogs(Long petId, Integer pageNum, Integer pageSize);
    Result<List<BehaviorLogDTO>> getAbnormalLogs(Long petId);
    Result<List<BehaviorLogDTO>> getLogsByTimeRange(Long petId, LocalDateTime start, LocalDateTime end);
    Result<Void> analyzeAndAlert(Long petId, String filePath);
}