package ynu.pet.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import ynu.pet.dto.BehaviorLogDTO;
import ynu.pet.dto.PageResult;
import ynu.pet.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ynu.pet.service.BehaviorLogService;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "行为监测", description = "宠物行为分析、异常检测")
@RestController
@RequestMapping("/api/behavior")
public class BehaviorLogController {

    @Autowired
    @Qualifier("behaviorLogServiceImpl")
    private BehaviorLogService logService;

    @Operation(summary = "记录行为数据", description = "手动录入或设备上报")
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/log")
    public Result<Void> createLog(@RequestBody BehaviorLogDTO dto) {
        return logService.createLog(dto);
    }

    @Operation(summary = "获取行为记录详情")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/{id}")
    public Result<BehaviorLogDTO> getLog(
            @Parameter(description = "记录ID") @PathVariable Long id) {
        return logService.getLogById(id);
    }

    @Operation(summary = "宠物行为记录列表", description = "分页查询某宠物的所有行为记录")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/pet/{petId}")
    public Result<PageResult<BehaviorLogDTO>> listLogs(
            @Parameter(description = "宠物ID") @PathVariable Long petId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize) {
        return logService.listLogs(petId, pageNum, pageSize);
    }

    @Operation(summary = "异常行为记录", description = "查询宠物的异常行为警报")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/pet/{petId}/abnormal")
    public Result<List<BehaviorLogDTO>> getAbnormalLogs(
            @Parameter(description = "宠物ID") @PathVariable Long petId) {
        return logService.getAbnormalLogs(petId);
    }

    @Operation(summary = "时间段查询", description = "查询指定时间范围内的行为记录")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/pet/{petId}/range")
    public Result<List<BehaviorLogDTO>> getLogsByTimeRange(
            @Parameter(description = "宠物ID") @PathVariable Long petId,
            @Parameter(description = "开始时间", example = "2024-01-01 00:00:00")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @Parameter(description = "结束时间", example = "2024-01-31 23:59:59")
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end) {
        return logService.getLogsByTimeRange(petId, start, end);
    }

    @Operation(summary = "AI行为分析", description = "上传视频/音频进行AI分析")
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/analyze")
    public Result<Void> analyze(
            @Parameter(description = "宠物ID") @RequestParam Long petId,
            @Parameter(description = "文件路径") @RequestParam String filePath) {
        return logService.analyzeAndAlert(petId, filePath);
    }
}