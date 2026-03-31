package com.atguigu.lease.web.app.controller.landlord;

import com.atguigu.lease.common.login.LoginUser;
import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.model.entity.PaymentType;
import com.atguigu.lease.model.enums.CreatorType;
import com.atguigu.lease.web.app.service.PaymentTypeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 房东支付方式管理控制器
 */
@Tag(name = "房东支付方式管理")
@RestController
@RequestMapping("/app/landlord/payment")
@Slf4j
public class LandlordPaymentTypeController {

    @Autowired
    private PaymentTypeService paymentTypeService;

    /**
     * 验证房东是否可以编辑支付方式
     */
    private boolean canEditPaymentType(PaymentType paymentType, Long landlordId) {
        // 只有房东自己创建的支付方式才能编辑
        return paymentType.getCreatorType() != null && 
               paymentType.getCreatorType() == 2 && 
               paymentType.getCreatorId() != null && 
               paymentType.getCreatorId().equals(landlordId);
    }

    @Operation(summary = "房东查询支付方式列表")
    @GetMapping("/list")
    public Result<List<PaymentType>> listPaymentType() {
        try {
            // 从LoginUserHolder获取当前登录用户信息（由拦截器设置）
            LoginUser loginUser = LoginUserHolder.getLoginUser();
            if (loginUser == null) {
                return Result.fail("用户未登录");
            }
            
            Long landlordId = loginUser.getUserId();
            log.info("房东查询支付方式列表：房东ID={}", landlordId);
            
            // 查询官方支付方式和房东自己创建的支付方式
            LambdaQueryWrapper<PaymentType> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(PaymentType::getIsDeleted, 0)
                       .and(wrapper -> wrapper
                           .eq(PaymentType::getCreatorType, CreatorType.OFFICIAL)
                           .or(subWrapper -> subWrapper
                               .eq(PaymentType::getCreatorType, CreatorType.LANDLORD)
                               .eq(PaymentType::getCreatorId, landlordId)
                           )
                       )
                       .orderByAsc(PaymentType::getCreatorType)
                       .orderByDesc(PaymentType::getCreateTime);
            
            List<PaymentType> list = paymentTypeService.list(queryWrapper);
            return Result.ok(list);
        } catch (Exception e) {
            log.error("查询支付方式列表失败", e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东新增或修改支付方式")
    @PostMapping("/saveOrUpdate")
    public Result saveOrUpdate(@RequestBody PaymentType paymentType, @RequestParam Long landlordId) {
        try {
            // 验证权限：房东只能修改自己创建的支付方式
            if (paymentType.getId() != null) {
                PaymentType existingPaymentType = paymentTypeService.getById(paymentType.getId());
                if (existingPaymentType == null) {
                    return Result.fail("支付方式不存在");
                }
                
                // 验证支付方式创建者
                if (!canEditPaymentType(existingPaymentType, landlordId)) {
                    return Result.fail("无权修改该支付方式");
                }
            } else {
                // 新增时设置创建者信息
                paymentType.setCreatorType(CreatorType.LANDLORD.getCode());
                paymentType.setCreatorId(landlordId);
            }
            
            paymentTypeService.saveOrUpdate(paymentType);
            
            log.info("房东保存支付方式成功：房东ID={}, 支付方式ID={}", landlordId, paymentType.getId());
            
            return Result.ok();
        } catch (Exception e) {
            log.error("房东保存支付方式失败", e);
            return Result.fail("保存支付方式失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东删除支付方式")
    @DeleteMapping("/deleteById")
    public Result removePaymentTypeById(@RequestParam Long id, @RequestParam Long landlordId) {
        try {
            // 验证支付方式是否存在
            PaymentType paymentType = paymentTypeService.getById(id);
            if (paymentType == null) {
                return Result.fail("支付方式不存在");
            }
            
            // 验证支付方式所有权（只能删除自己创建的）
            if (!canEditPaymentType(paymentType, landlordId)) {
                return Result.fail("无权删除该支付方式");
            }
            
            // 只有房东创建的支付方式才能被删除，官方支付方式不能删除
            if (paymentType.getCreatorType() == null || paymentType.getCreatorType() != 2) {
                return Result.fail("无法删除官方支付方式");
            }
            
            // 检查是否有房间正在使用该支付方式
            // TODO: 添加使用检查逻辑
            
            paymentTypeService.removeById(id);
            
            log.info("房东删除支付方式：房东ID={}, 支付方式ID={}", landlordId, id);
            
            return Result.ok();
        } catch (Exception e) {
            log.error("房东删除支付方式失败", e);
            return Result.fail("删除支付方式失败：" + e.getMessage());
        }
    }
}