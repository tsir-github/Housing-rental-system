package com.atguigu.lease.web.app.controller.landlord;

import com.atguigu.lease.common.login.LoginUser;
import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.model.entity.AttrKey;
import com.atguigu.lease.model.entity.AttrValue;
import com.atguigu.lease.model.enums.CreatorType;
import com.atguigu.lease.web.app.service.AttrKeyService;
import com.atguigu.lease.web.app.service.AttrValueService;
import com.atguigu.lease.web.app.vo.attr.AttrKeyVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 房东房间属性管理控制器
 */
@Tag(name = "房东房间属性管理")
@RestController
@RequestMapping("/app/landlord/attr")
@Slf4j
public class LandlordAttrController {

    @Autowired
    private AttrKeyService attrKeyService;
    
    @Autowired
    private AttrValueService attrValueService;

    /**
     * 验证房东是否可以编辑属性名称
     */
    private boolean canEditAttrKey(AttrKey attrKey, Long landlordId) {
        // 只有房东自己创建的属性名称才能编辑
        return attrKey.getCreatorType() != null && 
               attrKey.getCreatorType() == 2 && 
               attrKey.getCreatorId() != null && 
               attrKey.getCreatorId().equals(landlordId);
    }

    /**
     * 验证房东是否可以编辑属性值
     */
    private boolean canEditAttrValue(AttrValue attrValue, Long landlordId) {
        // 只有房东自己创建的属性值才能编辑
        return attrValue.getCreatorType() != null && 
               attrValue.getCreatorType() == 2 && 
               attrValue.getCreatorId() != null && 
               attrValue.getCreatorId().equals(landlordId);
    }

    @Operation(summary = "房东新增或更新属性名称")
    @PostMapping("/key/saveOrUpdate")
    public Result saveOrUpdateAttrKey(@RequestBody AttrKey attrKey, @RequestParam Long landlordId) {
        try {
            // 验证权限：房东只能修改自己创建的属性名称
            if (attrKey.getId() != null) {
                AttrKey existingAttrKey = attrKeyService.getById(attrKey.getId());
                if (existingAttrKey == null) {
                    return Result.fail("属性名称不存在");
                }
                
                // 验证属性名称创建者
                if (!canEditAttrKey(existingAttrKey, landlordId)) {
                    return Result.fail("无权修改该属性名称");
                }
            } else {
                // 新增时设置创建者信息
                attrKey.setCreatorType(CreatorType.LANDLORD.getCode());
                attrKey.setCreatorId(landlordId);
            }
            
            attrKeyService.saveOrUpdate(attrKey);
            
            log.info("房东保存属性名称成功：房东ID={}, 属性名称ID={}", landlordId, attrKey.getId());
            
            return Result.ok();
        } catch (Exception e) {
            log.error("房东保存属性名称失败", e);
            return Result.fail("保存属性名称失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东新增或更新属性值")
    @PostMapping("/value/saveOrUpdate")
    public Result saveOrUpdateAttrValue(@RequestBody AttrValue attrValue, @RequestParam Long landlordId) {
        try {
            // 验证权限：房东只能修改自己创建的属性值
            if (attrValue.getId() != null) {
                AttrValue existingAttrValue = attrValueService.getById(attrValue.getId());
                if (existingAttrValue == null) {
                    return Result.fail("属性值不存在");
                }
                
                // 验证属性值创建者
                if (!canEditAttrValue(existingAttrValue, landlordId)) {
                    return Result.fail("无权修改该属性值");
                }
            } else {
                // 新增时设置创建者信息
                attrValue.setCreatorType(CreatorType.LANDLORD.getCode());
                attrValue.setCreatorId(landlordId);
            }
            
            // 验证属性名称是否存在
            if (attrValue.getAttrKeyId() != null) {
                AttrKey attrKey = attrKeyService.getById(attrValue.getAttrKeyId());
                if (attrKey == null) {
                    return Result.fail("属性名称不存在");
                }
            }
            
            attrValueService.saveOrUpdate(attrValue);
            
            log.info("房东保存属性值成功：房东ID={}, 属性值ID={}", landlordId, attrValue.getId());
            
            return Result.ok();
        } catch (Exception e) {
            log.error("房东保存属性值失败", e);
            return Result.fail("保存属性值失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东查询全部属性名称和属性值列表")
    @GetMapping("/list")
    public Result<List<AttrKeyVo>> listAttrInfo() {
        try {
            // 从LoginUserHolder获取当前登录用户信息（由拦截器设置）
            LoginUser loginUser = LoginUserHolder.getLoginUser();
            if (loginUser == null) {
                return Result.fail("用户未登录");
            }
            
            Long landlordId = loginUser.getUserId();
            log.info("房东查询属性列表：房东ID={}", landlordId);
            
            // 查询所有属性（包括官方属性和房东自己创建的属性）
            List<AttrKeyVo> list = attrKeyService.listAttrInfo();
            return Result.ok(list);
        } catch (Exception e) {
            log.error("查询属性信息列表失败", e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东删除属性名称")
    @DeleteMapping("/key/deleteById")
    public Result removeAttrKeyById(@RequestParam Long attrKeyId, @RequestParam Long landlordId) {
        try {
            // 验证属性名称是否存在
            AttrKey attrKey = attrKeyService.getById(attrKeyId);
            if (attrKey == null) {
                return Result.fail("属性名称不存在");
            }
            
            // 验证属性名称所有权（只能删除自己创建的）
            if (!canEditAttrKey(attrKey, landlordId)) {
                return Result.fail("无权删除该属性名称");
            }
            
            // 只有房东创建的属性名称才能被删除，官方属性名称不能删除
            if (attrKey.getCreatorType() == null || attrKey.getCreatorType() != 2) {
                return Result.fail("无法删除官方属性名称");
            }
            
            // 检查是否有房间正在使用该属性
            // TODO: 添加使用检查逻辑
            
            // 删除属性名称
            attrKeyService.removeById(attrKeyId);
            
            // 删除相关属性值
            LambdaQueryWrapper<AttrValue> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AttrValue::getAttrKeyId, attrKeyId);
            attrValueService.remove(queryWrapper);
            
            log.info("房东删除属性名称：房东ID={}, 属性名称ID={}", landlordId, attrKeyId);
            
            return Result.ok();
        } catch (Exception e) {
            log.error("房东删除属性名称失败", e);
            return Result.fail("删除属性名称失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东删除属性值")
    @DeleteMapping("/value/deleteById")
    public Result removeAttrValueById(@RequestParam Long id, @RequestParam Long landlordId) {
        try {
            // 验证属性值是否存在
            AttrValue attrValue = attrValueService.getById(id);
            if (attrValue == null) {
                return Result.fail("属性值不存在");
            }
            
            // 验证属性值所有权（只能删除自己创建的）
            if (!canEditAttrValue(attrValue, landlordId)) {
                return Result.fail("无权删除该属性值");
            }
            
            // 只有房东创建的属性值才能被删除，官方属性值不能删除
            if (attrValue.getCreatorType() == null || attrValue.getCreatorType() != 2) {
                return Result.fail("无法删除官方属性值");
            }
            
            // 检查是否有房间正在使用该属性值
            // TODO: 添加使用检查逻辑
            
            attrValueService.removeById(id);
            
            log.info("房东删除属性值：房东ID={}, 属性值ID={}", landlordId, id);
            
            return Result.ok();
        } catch (Exception e) {
            log.error("房东删除属性值失败", e);
            return Result.fail("删除属性值失败：" + e.getMessage());
        }
    }
}