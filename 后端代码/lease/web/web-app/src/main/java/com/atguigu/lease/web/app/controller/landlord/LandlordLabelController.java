package com.atguigu.lease.web.app.controller.landlord;

import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.model.entity.LabelInfo;
import com.atguigu.lease.model.enums.CreatorType;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.app.service.LabelInfoService;
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
 * 房东标签管理控制器
 */
@Tag(name = "房东标签管理")
@RestController
@RequestMapping("/app/landlord/label")
@Slf4j
public class LandlordLabelController {

    @Autowired
    private LabelInfoService labelInfoService;

    @Operation(summary = "房东查询标签列表")
    @GetMapping("/list")
    public Result<List<LabelInfo>> labelList(@RequestParam(required = false) ItemType type,
                                            HttpServletRequest request) {
        try {
            Long landlordId = LoginUserHolder.getLoginUser().getUserId();
            
            List<LabelInfo> list;
            if (type != null) {
                // 根据类型获取标签列表（包含系统默认和房东创建的）
                list = labelInfoService.listByTypeAndLandlord(type, landlordId);
            } else {
                // 获取所有类型的标签（官方创建的 + 房东创建的）
                LambdaQueryWrapper<LabelInfo> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(LabelInfo::getIsDeleted, 0)
                           .and(wrapper -> wrapper
                               .eq(LabelInfo::getCreatorType, CreatorType.OFFICIAL)
                               .or(subWrapper -> subWrapper
                                   .eq(LabelInfo::getCreatorType, CreatorType.LANDLORD)
                                   .eq(LabelInfo::getCreatorId, landlordId)
                               )
                           )
                           .orderByDesc(LabelInfo::getCreateTime);
                list = labelInfoService.list(queryWrapper);
            }
            
            return Result.ok(list);
        } catch (Exception e) {
            log.error("查询标签列表失败", e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东新增或修改标签信息")
    @PostMapping("/saveOrUpdate")
    public Result saveOrUpdateLabel(@RequestBody LabelInfo labelInfo, @RequestParam Long landlordId) {
        try {
            // 验证权限：房东只能修改自己创建的标签信息
            if (labelInfo.getId() != null) {
                LabelInfo existingLabel = labelInfoService.getById(labelInfo.getId());
                if (existingLabel == null) {
                    return Result.fail("标签信息不存在");
                }
                
                // 验证标签信息创建者
                if (CreatorType.LANDLORD.equals(existingLabel.getCreatorType()) && 
                    !existingLabel.getCreatorId().equals(landlordId)) {
                    return Result.fail("无权修改该标签信息");
                }
            } else {
                // 新增时设置创建者信息
                labelInfo.setCreatorId(landlordId);
                labelInfo.setCreatorType(CreatorType.LANDLORD);
            }
            
            labelInfoService.saveOrUpdate(labelInfo);
            
            log.info("房东保存标签信息成功：房东ID={}, 标签ID={}", landlordId, labelInfo.getId());
            
            return Result.ok();
        } catch (Exception e) {
            log.error("房东保存标签信息失败", e);
            return Result.fail("保存标签信息失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东删除标签信息")
    @DeleteMapping("/deleteById")
    public Result deleteLabelById(@RequestParam Long id, @RequestParam Long landlordId) {
        try {
            // 验证标签信息是否存在
            LabelInfo labelInfo = labelInfoService.getById(id);
            if (labelInfo == null) {
                return Result.fail("标签信息不存在");
            }
            
            // 验证标签信息所有权（只能删除自己创建的）
            if (CreatorType.LANDLORD.equals(labelInfo.getCreatorType()) && 
                !labelInfo.getCreatorId().equals(landlordId)) {
                return Result.fail("无权删除该标签信息");
            }
            
            // 只有房东创建的标签才能被删除，官方标签不能删除
            if (!CreatorType.LANDLORD.equals(labelInfo.getCreatorType())) {
                return Result.fail("无法删除官方标签");
            }
            
            // 检查是否有公寓或房间正在使用该标签
            // TODO: 添加使用检查逻辑
            
            labelInfoService.removeById(id);
            
            log.info("房东删除标签信息：房东ID={}, 标签ID={}", landlordId, id);
            
            return Result.ok();
        } catch (Exception e) {
            log.error("房东删除标签信息失败", e);
            return Result.fail("删除标签信息失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东查询公寓标签列表")
    @GetMapping("/listApartmentLabels")
    public Result<List<LabelInfo>> listApartmentLabels() {
        try {
            LambdaQueryWrapper<LabelInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(LabelInfo::getType, ItemType.APARTMENT);
            queryWrapper.eq(LabelInfo::getIsDeleted, 0);
            queryWrapper.orderByDesc(LabelInfo::getCreateTime);
            
            List<LabelInfo> list = labelInfoService.list(queryWrapper);
            return Result.ok(list);
        } catch (Exception e) {
            log.error("查询公寓标签列表失败", e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东查询房间标签列表")
    @GetMapping("/listRoomLabels")
    public Result<List<LabelInfo>> listRoomLabels() {
        try {
            LambdaQueryWrapper<LabelInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(LabelInfo::getType, ItemType.ROOM);
            queryWrapper.eq(LabelInfo::getIsDeleted, 0);
            queryWrapper.orderByDesc(LabelInfo::getCreateTime);
            
            List<LabelInfo> list = labelInfoService.list(queryWrapper);
            return Result.ok(list);
        } catch (Exception e) {
            log.error("查询房间标签列表失败", e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }
}