package com.atguigu.lease.web.app.controller.landlord;

import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.model.entity.FacilityInfo;
import com.atguigu.lease.model.enums.CreatorType;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.app.service.FacilityInfoService;
import com.atguigu.lease.common.login.LoginUserHolder;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 房东配套管理控制器
 */
@Tag(name = "房东配套管理")
@RestController
@RequestMapping("/app/landlord/facility")
@Slf4j
public class LandlordFacilityController {

    @Autowired
    private FacilityInfoService facilityInfoService;

    @Operation(summary = "房东查询配套信息列表")
    @GetMapping("/list")
    public Result<List<FacilityInfo>> listFacility(@RequestParam(required = false) ItemType type,
                                                   HttpServletRequest request) {
        try {
            Long landlordId = LoginUserHolder.getLoginUser().getUserId();
            
            List<FacilityInfo> list;
            if (type != null) {
                // 根据类型获取配套设施列表（包含系统默认和房东创建的）
                list = facilityInfoService.listByTypeAndLandlord(type, landlordId);
            } else {
                // 获取所有类型的配套设施（官方创建的 + 房东创建的）
                LambdaQueryWrapper<FacilityInfo> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(FacilityInfo::getIsDeleted, 0)
                           .and(wrapper -> wrapper
                               .eq(FacilityInfo::getCreatorType, CreatorType.OFFICIAL)
                               .or(subWrapper -> subWrapper
                                   .eq(FacilityInfo::getCreatorType, CreatorType.LANDLORD)
                                   .eq(FacilityInfo::getCreatorId, landlordId)
                               )
                           )
                           .orderByDesc(FacilityInfo::getCreateTime);
                list = facilityInfoService.list(queryWrapper);
            }
            
            return Result.ok(list);
        } catch (Exception e) {
            log.error("查询配套信息列表失败", e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东新增或修改配套信息")
    @PostMapping("/saveOrUpdate")
    public Result saveOrUpdate(@RequestBody FacilityInfo facilityInfo, @RequestParam Long landlordId) {
        try {
            // 验证权限：房东只能修改自己创建的配套信息
            if (facilityInfo.getId() != null) {
                FacilityInfo existingFacility = facilityInfoService.getById(facilityInfo.getId());
                if (existingFacility == null) {
                    return Result.fail("配套信息不存在");
                }
                
                // 验证配套信息创建者
                if (CreatorType.LANDLORD.equals(existingFacility.getCreatorType()) && 
                    !existingFacility.getCreatorId().equals(landlordId)) {
                    return Result.fail("无权修改该配套信息");
                }
            }
            
            // 设置创建者信息
            facilityInfo.setCreatorId(landlordId);
            facilityInfo.setCreatorType(CreatorType.LANDLORD);
            
            facilityInfoService.saveOrUpdate(facilityInfo);
            
            log.info("房东保存配套信息成功：房东ID={}, 配套ID={}", landlordId, facilityInfo.getId());
            
            return Result.ok();
        } catch (Exception e) {
            log.error("房东保存配套信息失败", e);
            return Result.fail("保存配套信息失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东删除配套信息")
    @DeleteMapping("/deleteById")
    public Result removeFacilityById(@RequestParam Long id, @RequestParam Long landlordId) {
        try {
            // 验证配套信息是否存在
            FacilityInfo facilityInfo = facilityInfoService.getById(id);
            if (facilityInfo == null) {
                return Result.fail("配套信息不存在");
            }
            
            // 验证配套信息所有权（只能删除自己创建的）
            if (CreatorType.LANDLORD.equals(facilityInfo.getCreatorType()) && 
                !facilityInfo.getCreatorId().equals(landlordId)) {
                return Result.fail("无权删除该配套信息");
            }
            
            // 系统默认配套不能删除
            if (CreatorType.SYSTEM.equals(facilityInfo.getCreatorType())) {
                return Result.fail("系统默认配套不能删除");
            }
            
            // 检查是否有公寓或房间正在使用该配套
            // TODO: 添加使用检查逻辑
            
            facilityInfoService.removeById(id);
            
            log.info("房东删除配套信息：房东ID={}, 配套ID={}", landlordId, id);
            
            return Result.ok();
        } catch (Exception e) {
            log.error("房东删除配套信息失败", e);
            return Result.fail("删除配套信息失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东查询公寓配套信息列表")
    @GetMapping("/listApartmentFacilities")
    public Result<List<FacilityInfo>> listApartmentFacilities() {
        try {
            Long landlordId = LoginUserHolder.getLoginUser().getUserId();
            List<FacilityInfo> list = facilityInfoService.listByTypeAndLandlord(ItemType.APARTMENT, landlordId);
            return Result.ok(list);
        } catch (Exception e) {
            log.error("查询公寓配套信息列表失败", e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东查询房间配套信息列表")
    @GetMapping("/listRoomFacilities")
    public Result<List<FacilityInfo>> listRoomFacilities() {
        try {
            Long landlordId = LoginUserHolder.getLoginUser().getUserId();
            List<FacilityInfo> list = facilityInfoService.listByTypeAndLandlord(ItemType.ROOM, landlordId);
            return Result.ok(list);
        } catch (Exception e) {
            log.error("查询房间配套信息列表失败", e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }
}