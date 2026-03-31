package com.atguigu.lease.web.app.controller.landlord;

import com.atguigu.lease.common.login.LoginUser;
import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.model.entity.FeeKey;
import com.atguigu.lease.model.entity.FeeValue;
import com.atguigu.lease.model.enums.CreatorType;
import com.atguigu.lease.web.app.service.FeeKeyService;
import com.atguigu.lease.web.app.service.FeeValueService;
import com.atguigu.lease.web.app.vo.fee.FeeKeyVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 房东杂费管理控制器
 */
@Tag(name = "房东杂费管理")
@RestController
@RequestMapping("/app/landlord/fee")
@Slf4j
public class LandlordFeeController {

    @Autowired
    private FeeKeyService feeKeyService;
    
    @Autowired
    private FeeValueService feeValueService;

    /**
     * 验证房东是否可以编辑杂费键
     */
    private boolean canEditFeeKey(FeeKey feeKey, Long landlordId) {
        // 只有房东自己创建的杂费键才能编辑
        return feeKey.getCreatorType() != null && 
               feeKey.getCreatorType() == 2 && 
               feeKey.getCreatorId() != null && 
               feeKey.getCreatorId().equals(landlordId);
    }

    /**
     * 验证房东是否可以编辑杂费值
     */
    private boolean canEditFeeValue(FeeValue feeValue, Long landlordId) {
        // 只有房东自己创建的杂费值才能编辑
        return feeValue.getCreatorType() != null && 
               feeValue.getCreatorType() == 2 && 
               feeValue.getCreatorId() != null && 
               feeValue.getCreatorId().equals(landlordId);
    }

    @Operation(summary = "房东新增或更新杂费键")
    @PostMapping("/key/saveOrUpdate")
    public Result saveOrUpdateFeeKey(@RequestBody FeeKey feeKey, @RequestParam Long landlordId) {
        try {
            // 验证权限：房东只能修改自己创建的杂费键
            if (feeKey.getId() != null) {
                FeeKey existingFeeKey = feeKeyService.getById(feeKey.getId());
                if (existingFeeKey == null) {
                    return Result.fail("杂费类型不存在");
                }
                
                // 验证杂费键创建者
                if (!canEditFeeKey(existingFeeKey, landlordId)) {
                    return Result.fail("无权修改该杂费类型");
                }
            } else {
                // 新增时设置创建者信息
                feeKey.setCreatorType(CreatorType.LANDLORD.getCode());
                feeKey.setCreatorId(landlordId);
            }
            
            feeKeyService.saveOrUpdate(feeKey);
            
            log.info("房东保存杂费键成功：房东ID={}, 杂费键ID={}", landlordId, feeKey.getId());
            
            return Result.ok();
        } catch (Exception e) {
            log.error("房东保存杂费键失败", e);
            return Result.fail("保存杂费类型失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东新增或更新杂费值")
    @PostMapping("/value/saveOrUpdate")
    public Result saveOrUpdateFeeValue(@RequestBody FeeValue feeValue, @RequestParam Long landlordId) {
        try {
            // 验证权限：房东只能修改自己创建的杂费值
            if (feeValue.getId() != null) {
                FeeValue existingFeeValue = feeValueService.getById(feeValue.getId());
                if (existingFeeValue == null) {
                    return Result.fail("杂费值不存在");
                }
                
                // 验证杂费值创建者
                if (!canEditFeeValue(existingFeeValue, landlordId)) {
                    return Result.fail("无权修改该杂费值");
                }
            } else {
                // 新增时设置创建者信息
                feeValue.setCreatorType(CreatorType.LANDLORD.getCode());
                feeValue.setCreatorId(landlordId);
            }
            
            // 验证杂费键是否存在
            if (feeValue.getFeeKeyId() != null) {
                FeeKey feeKey = feeKeyService.getById(feeValue.getFeeKeyId());
                if (feeKey == null) {
                    return Result.fail("杂费类型不存在");
                }
            }
            
            feeValueService.saveOrUpdate(feeValue);
            
            log.info("房东保存杂费值成功：房东ID={}, 杂费值ID={}", landlordId, feeValue.getId());
            
            return Result.ok();
        } catch (Exception e) {
            log.error("房东保存杂费值失败", e);
            return Result.fail("保存杂费值失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东查询全部杂费键和杂费值列表")
    @GetMapping("/list")
    public Result<List<FeeKeyVo>> listFeeInfo(@RequestParam(required = false) Long landlordId) {
        try {
            // 如果没有传递landlordId，从LoginUserHolder获取当前登录用户信息
            if (landlordId == null) {
                LoginUser loginUser = LoginUserHolder.getLoginUser();
                if (loginUser == null) {
                    return Result.fail("用户未登录");
                }
                landlordId = loginUser.getUserId();
            }
            
            log.info("房东查询杂费列表：房东ID={}", landlordId);
            
            // 查询所有杂费（包括官方杂费和房东自己创建的杂费）
            List<FeeKeyVo> list = feeKeyService.listFeeInfo(landlordId);
            return Result.ok(list);
        } catch (Exception e) {
            log.error("查询杂费信息列表失败", e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东删除杂费键")
    @DeleteMapping("/key/deleteById")
    public Result removeFeeKeyById(@RequestParam Long feeKeyId, @RequestParam Long landlordId) {
        try {
            // 验证杂费键是否存在
            FeeKey feeKey = feeKeyService.getById(feeKeyId);
            if (feeKey == null) {
                return Result.fail("杂费类型不存在");
            }
            
            // 验证杂费键所有权（只能删除自己创建的）
            if (!canEditFeeKey(feeKey, landlordId)) {
                return Result.fail("无权删除该杂费类型");
            }
            
            // 只有房东创建的杂费键才能被删除，官方杂费键不能删除
            if (feeKey.getCreatorType() == null || feeKey.getCreatorType() != 2) {
                return Result.fail("无法删除官方杂费类型");
            }
            
            // 检查是否有房间正在使用该杂费
            // TODO: 添加使用检查逻辑
            
            // 删除杂费键
            feeKeyService.removeById(feeKeyId);
            
            // 删除相关杂费值
            LambdaQueryWrapper<FeeValue> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(FeeValue::getFeeKeyId, feeKeyId);
            feeValueService.remove(queryWrapper);
            
            log.info("房东删除杂费键：房东ID={}, 杂费键ID={}", landlordId, feeKeyId);
            
            return Result.ok();
        } catch (Exception e) {
            log.error("房东删除杂费键失败", e);
            return Result.fail("删除杂费类型失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东删除杂费值")
    @DeleteMapping("/value/deleteById")
    public Result removeFeeValueById(@RequestParam Long id, @RequestParam Long landlordId) {
        try {
            // 验证杂费值是否存在
            FeeValue feeValue = feeValueService.getById(id);
            if (feeValue == null) {
                return Result.fail("杂费值不存在");
            }
            
            // 验证杂费值所有权（只能删除自己创建的）
            if (!canEditFeeValue(feeValue, landlordId)) {
                return Result.fail("无权删除该杂费值");
            }
            
            // 只有房东创建的杂费值才能被删除，官方杂费值不能删除
            if (feeValue.getCreatorType() == null || feeValue.getCreatorType() != 2) {
                return Result.fail("无法删除官方杂费值");
            }
            
            // 检查是否有房间正在使用该杂费值
            // TODO: 添加使用检查逻辑
            
            feeValueService.removeById(id);
            
            log.info("房东删除杂费值：房东ID={}, 杂费值ID={}", landlordId, id);
            
            return Result.ok();
        } catch (Exception e) {
            log.error("房东删除杂费值失败", e);
            return Result.fail("删除杂费值失败：" + e.getMessage());
        }
    }
}