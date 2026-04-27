package ynu.pet.service.impl;

import ynu.pet.dto.BehaviorLogDTO;
import ynu.pet.dto.PageResult;
import ynu.pet.dto.Result;
import ynu.pet.entity.BehaviorLog;
import ynu.pet.entity.BehaviorModel;
import ynu.pet.entity.Pet;
import ynu.pet.entity.Image;
import ynu.pet.mapper.BehaviorLogMapper;
import ynu.pet.mapper.BehaviorModelMapper;
import ynu.pet.mapper.ImageMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ynu.pet.service.BehaviorLogService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class BehaviorLogServiceImpl implements BehaviorLogService {

    @Autowired
    private BehaviorLogMapper logMapper;

    @Autowired
    private BehaviorModelMapper modelMapper;

    @Autowired
    private ImageMapper imageMapper;

    @Override
    public Result<Void> createLog(BehaviorLogDTO dto) {
        BehaviorLog log = new BehaviorLog();
        BeanUtils.copyProperties(dto, log);

        Pet pet = new Pet();
        pet.setId(dto.getPetId());
        log.setPet(pet);

        logMapper.insert(log);
        return Result.success();
    }

    @Override
    public Result<BehaviorLogDTO> getLogById(Long id) {
        BehaviorLog log = logMapper.selectById(id);
        if (log == null) {
            return Result.error("记录不存在");
        }
        return Result.success(convertToDTO(log));
    }

    @Override
    public Result<PageResult<BehaviorLogDTO>> listLogs(Long petId, Integer pageNum, Integer pageSize) {
        List<BehaviorLog> logs = logMapper.selectByPetId(petId);
        List<BehaviorLogDTO> dtoList = new ArrayList<>();

        for (BehaviorLog log : logs) {
            dtoList.add(convertToDTO(log));
        }

        PageResult<BehaviorLogDTO> pageResult = new PageResult<>((long) dtoList.size(), dtoList, pageNum, pageSize);
        return Result.success(pageResult);
    }

    @Override
    public Result<List<BehaviorLogDTO>> getAbnormalLogs(Long petId) {
        List<BehaviorLog> logs = logMapper.selectAbnormalByPetId(petId);
        List<BehaviorLogDTO> dtoList = new ArrayList<>();

        for (BehaviorLog log : logs) {
            dtoList.add(convertToDTO(log));
        }

        return Result.success(dtoList);
    }

    @Override
    public Result<List<BehaviorLogDTO>> getLogsByTimeRange(Long petId, LocalDateTime start, LocalDateTime end) {
        List<BehaviorLog> logs = logMapper.selectByTimeRange(petId, start, end);
        List<BehaviorLogDTO> dtoList = new ArrayList<>();

        for (BehaviorLog log : logs) {
            dtoList.add(convertToDTO(log));
        }

        return Result.success(dtoList);
    }

    @Override
    public Result<Void> analyzeAndAlert(Long petId, String filePath) {
        // 调用AI模型分析
        // 这里简化处理，实际调用Python服务或加载模型

        BehaviorLog log = new BehaviorLog();
        Pet pet = new Pet();
        pet.setId(petId);
        log.setPet(pet);

        // 获取活跃模型
        List<BehaviorModel> models = modelMapper.selectActiveModels();
        if (!models.isEmpty()) {
            log.setModel(models.get(0));
        }

        log.setRecordTime(LocalDateTime.now());
        log.setBehaviorType("normal");
        log.setEmotionType("calm");
        log.setAbnormalAlert(false);

        logMapper.insert(log);

        return Result.success();
    }

    private BehaviorLogDTO convertToDTO(BehaviorLog log) {
        BehaviorLogDTO dto = new BehaviorLogDTO();
        BeanUtils.copyProperties(log, dto);

        if (log.getPet() != null) {
            dto.setPetId(log.getPet().getId());
            dto.setPetName(log.getPet().getPetName());
        }

        // 查询图片
        List<Image> images = imageMapper.selectByBehaviorLogId(log.getId());
        List<String> imageUrls = new ArrayList<>();
        for (Image img : images) {
            imageUrls.add(img.getImageUrl());
        }
        dto.setImages(imageUrls);

        return dto;
    }
}