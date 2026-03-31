package com.atguigu.lease.web.app.controller.landlord;

import com.atguigu.lease.common.login.LoginUser;
import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.model.entity.LeaseTerm;
import com.atguigu.lease.model.enums.CreatorType;
import com.atguigu.lease.web.app.service.LeaseTermService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 房东租期管理控制器
 */
@Tag(name = "房东租期管理")
@RestController
@RequestMapping("/app/landlord/lease-term")
@Slf4j
public class LandlordLeaseTermController {

    @Autowired
    private LeaseTermService leaseTermService;

    /**
     * 验证房东是否可以编辑租期
     */
    private boolean canEditLeaseTerm(LeaseTerm leaseTerm, Long landlordId) {
        // 只有房东自己创建的租期才能编辑
        return leaseTerm.getCreatorType() != null && 
               leaseTerm.getCreatorType() == 2 && 
               leaseTerm.getCreatorId() != null && 
               leaseTerm.getCreatorId().equals(landlordId);
    }

    @Operation(summary = "房东查询租期列表")
    @GetMapping("/list")
    public Result<List<LeaseTerm>> listLeaseTerm() {
        try {
            // 从LoginUserHolder获取当前登录用户信息（由拦截器设置）
            LoginUser loginUser = LoginUserHolder.getLoginUser();
            if (loginUser == null) {
                return Result.fail("用户未登录");
            }
            
            Long landlordId = loginUser.getUserId();
            log.info("房东查询租期列表：房东ID={}", landlordId);
            
            // 查询官方租期和房东自己创建的租期
            LambdaQueryWrapper<LeaseTerm> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(LeaseTerm::getIsDeleted, 0)
                       .and(wrapper -> wrapper
                           .eq(LeaseTerm::getCreatorType, CreatorType.OFFICIAL)
                           .or(subWrapper -> subWrapper
                               .eq(LeaseTerm::getCreatorType, CreatorType.LANDLORD)
                               .eq(LeaseTerm::getCreatorId, landlordId)
                           )
                       )
                       .orderByAsc(LeaseTerm::getCreatorType)
                       .orderByAsc(LeaseTerm::getMonthCount);
            
            List<LeaseTerm> list = leaseTermService.list(queryWrapper);
            return Result.ok(list);
        } catch (Exception e) {
            log.error("查询租期列表失败", e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东新增或修改租期")
    @PostMapping("/saveOrUpdate")
    public Result saveOrUpdate(@RequestBody LeaseTerm leaseTerm, @RequestParam Long landlordId) {
        try {
            // 验证权限：房东只能修改自己创建的租期
            if (leaseTerm.getId() != null) {
                LeaseTerm existingLeaseTerm = leaseTermService.getById(leaseTerm.getId());
                if (existingLeaseTerm == null) {
                    return Result.fail("租期不存在");
                }
                
                // 验证租期创建者
                if (!canEditLeaseTerm(existingLeaseTerm, landlordId)) {
                    return Result.fail("无权修改该租期");
                }
            } else {
                // 新增时设置创建者信息
                leaseTerm.setCreatorType(CreatorType.LANDLORD.getCode());
                leaseTerm.setCreatorId(landlordId);
            }
            
            // 验证租期月数的合理性
            if (leaseTerm.getMonthCount() == null || leaseTerm.getMonthCount() <= 0) {
                return Result.fail("租期月数必须大于0");
            }
            
            leaseTermService.saveOrUpdate(leaseTerm);
            
            log.info("房东保存租期成功：房东ID={}, 租期ID={}", landlordId, leaseTerm.getId());
            
            return Result.ok();
        } catch (Exception e) {
            log.error("房东保存租期失败", e);
            return Result.fail("保存租期失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东删除租期")
    @DeleteMapping("/deleteById")
    public Result removeLeaseTermById(@RequestParam Long id, @RequestParam Long landlordId) {
        try {
            // 验证租期是否存在
            LeaseTerm leaseTerm = leaseTermService.getById(id);
            if (leaseTerm == null) {
                return Result.fail("租期不存在");
            }
            
            // 验证租期所有权（只能删除自己创建的）
            if (!canEditLeaseTerm(leaseTerm, landlordId)) {
                return Result.fail("无权删除该租期");
            }
            
            // 只有房东创建的租期才能被删除，官方租期不能删除
            if (leaseTerm.getCreatorType() == null || leaseTerm.getCreatorType() != 2) {
                return Result.fail("无法删除官方租期");
            }
            
            // 检查是否有房间正在使用该租期
            // TODO: 添加使用检查逻辑
            
            leaseTermService.removeById(id);
            
            log.info("房东删除租期：房东ID={}, 租期ID={}", landlordId, id);
            
            return Result.ok();
        } catch (Exception e) {
            log.error("房东删除租期失败", e);
            return Result.fail("删除租期失败：" + e.getMessage());
        }
    }
}