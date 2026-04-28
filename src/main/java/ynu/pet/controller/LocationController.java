package ynu.pet.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ynu.pet.dto.LocationDTO;
import ynu.pet.dto.Result;
import ynu.pet.service.PetLocationService;

import java.util.List;
import java.util.Map;

@Tag(name = "宠物定位", description = "宠物实时位置追踪相关接口")
@RestController
@RequestMapping("/api/location")
public class LocationController {

    @Autowired
    private PetLocationService locationService;

    @Operation(summary = "获取宠物最新位置")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/{petId}/latest")
    public Result<LocationDTO> getLatestLocation(
            @Parameter(description = "宠物ID") @PathVariable Long petId) {
        return locationService.getLatestLocation(petId);
    }

    @Operation(summary = "获取宠物历史轨迹", description = "获取宠物过去N小时的位置历史，最多72小时")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/{petId}/history")
    public Result<List<LocationDTO>> getHistory(
            @Parameter(description = "宠物ID") @PathVariable Long petId,
            @Parameter(description = "过去多少小时，默认24") @RequestParam(defaultValue = "24") int hours) {
        return locationService.getLocationHistory(petId, hours);
    }

    @Operation(summary = "真实设备上报接口", description = "供 GPS 硬件设备调用，上报当前位置坐标")
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/{petId}/report")
    public Result<Void> reportLocation(
            @Parameter(description = "宠物ID") @PathVariable Long petId,
            @RequestBody Map<String, Double> body) {
        Double lat = body.get("latitude");
        Double lng = body.get("longitude");
        if (lat == null || lng == null) {
            return Result.error("latitude 和 longitude 不能为空");
        }
        return locationService.reportLocation(petId, lat, lng);
    }
}
