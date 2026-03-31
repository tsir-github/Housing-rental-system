package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.model.entity.UserInfo;
import com.atguigu.lease.model.entity.LandlordPermission;
import com.atguigu.lease.model.enums.BaseStatus;
import com.atguigu.lease.web.admin.mapper.UserInfoMapper;
import com.atguigu.lease.web.admin.mapper.LandlordPermissionMapper;
import com.atguigu.lease.web.admin.service.LandlordPermissionService;
import com.atguigu.lease.web.admin.vo.landlord.LandlordPermissionVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

/**
 * 后台管理房东权限服务实现类（简化版）
 */
@Service
@Slf4j
public class LandlordPermissionServiceImpl implements LandlordPermissionService {

    @Autowired
    private UserInfoMapper userInfoMapper;
    
    @Autowired
    private LandlordPermissionMapper landlordPermissionMapper;

    // 所有可分配的房东权限
    private static final List<String> AVAILABLE_PERMISSIONS = Arrays.asList(
        // 基础权限
        "manage_rooms",           // 管理房源（发布、编辑、下架）
        "manage_apartments",      // 管理公寓信息
        "manage_appointments",    // 管理预约看房
        "create_lease",          // 创建租约合同
        
        // 房源管理相关权限
        "manage_fees",           // 房间杂费管理
        "manage_facilities",     // 配套管理
        "manage_attributes",     // 房间属性管理
        "manage_lease_terms",    // 租期管理
        "manage_payment_types",  // 支付方式管理
        "manage_labels",         // 标签管理
        "manage_files",          // 文件管理
        "manage_regions",        // 地区信息管理
        
        // 高级权限
        "view_applications",     // 查看直接入住申请
        "process_applications",  // 处理直接入住申请
        "view_statistics",       // 查看房东数据统计
        "manage_tenants",        // 管理租客信息
        "financial_management",  // 财务管理（收租、账单等）
        "view_reviews",          // 查看房源审核状态
        "manage_reviews"         // 房源审核管理
    );

    @Override
    public IPage<LandlordPermissionVo> getLandlordList(Long current, Long size, String phone, String nickname) {
        log.info("查询房东列表: current={}, size={}, phone={}, nickname={}", current, size, phone, nickname);
        
        // 先查询所有有房东权限的用户ID（去重）
        List<Long> landlordIds = landlordPermissionMapper.selectDistinctUserIds();
        log.info("找到房东用户ID列表: {}", landlordIds);
        
        if (landlordIds.isEmpty()) {
            // 如果没有房东，返回空结果
            Page<LandlordPermissionVo> emptyPage = new Page<>(current, size);
            emptyPage.setRecords(new ArrayList<>());
            emptyPage.setTotal(0);
            return emptyPage;
        }
        
        // 构建查询条件，只查询有房东权限的用户
        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(UserInfo::getId, landlordIds);
        queryWrapper.eq(UserInfo::getIsDeleted, 0);
        // 不限制状态，允许查看被禁用的房东
        
        if (StringUtils.hasText(phone)) {
            queryWrapper.like(UserInfo::getPhone, phone);
        }
        if (StringUtils.hasText(nickname)) {
            queryWrapper.like(UserInfo::getNickname, nickname);
        }
        
        queryWrapper.orderByDesc(UserInfo::getCreateTime);
        
        // 分页查询房东用户
        Page<UserInfo> page = new Page<>(current, size);
        IPage<UserInfo> userPage = userInfoMapper.selectPage(page, queryWrapper);
        
        log.info("查询到房东用户数量: {}", userPage.getRecords().size());
        
        // 转换为VO并填充权限信息
        IPage<LandlordPermissionVo> result = userPage.convert(user -> {
            LandlordPermissionVo vo = new LandlordPermissionVo();
            vo.setLandlordId(user.getId());
            vo.setPhone(user.getPhone());
            vo.setNickname(user.getNickname());
            vo.setAvatarUrl(user.getAvatarUrl());
            vo.setStatus(user.getStatus().getCode());
            vo.setCreateTime(user.getCreateTime());
            vo.setUpdateTime(user.getUpdateTime());
            
            // 查询该房东的具体权限
            List<String> permissions = landlordPermissionMapper.selectPermissionsByUserId(user.getId());
            vo.setPermissions(permissions != null ? permissions : new ArrayList<>());
            vo.setPermissionCount(permissions != null ? permissions.size() : 0);
            vo.setHasLandlordPermission(permissions != null && !permissions.isEmpty());
            
            log.info("房东信息: ID={}, 昵称={}, 权限数量={}", user.getId(), user.getNickname(), vo.getPermissionCount());
            
            return vo;
        });
        
        return result;
    }

    @Override
    public List<String> getLandlordPermissions(Long landlordId) {
        try {
            // 从数据库查询房东的实际权限
            List<String> permissions = landlordPermissionMapper.selectPermissionsByUserId(landlordId);
            log.info("查询房东权限: landlordId={}, permissions={}", landlordId, permissions);
            return permissions != null ? permissions : new ArrayList<>();
        } catch (Exception e) {
            log.error("查询房东权限失败: landlordId={}", landlordId, e);
            return new ArrayList<>();
        }
    }

    @Override
    @Transactional
    public void assignPermissions(Long landlordId, List<String> permissions) {
        // 验证用户存在
        UserInfo user = userInfoMapper.selectById(landlordId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 验证权限有效性
        for (String permission : permissions) {
            if (!AVAILABLE_PERMISSIONS.contains(permission)) {
                throw new RuntimeException("无效的权限代码: " + permission);
            }
        }
        
        log.info("为房东分配权限: landlordId={}, permissions={}", landlordId, permissions);
        
        // 实际的权限分配逻辑
        for (String permission : permissions) {
            // 检查权限是否已存在，避免重复插入
            LambdaQueryWrapper<LandlordPermission> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(LandlordPermission::getUserId, landlordId)
                       .eq(LandlordPermission::getPermissionCode, permission)
                       .eq(LandlordPermission::getIsDeleted, 0);
            
            LandlordPermission existingPermission = landlordPermissionMapper.selectOne(queryWrapper);
            
            if (existingPermission == null) {
                // 权限不存在，插入新权限
                LandlordPermission newPermission = new LandlordPermission();
                newPermission.setUserId(landlordId);
                newPermission.setPermissionCode(permission);
                landlordPermissionMapper.insert(newPermission);
                log.info("成功分配权限: userId={}, permission={}", landlordId, permission);
            } else {
                log.info("权限已存在，跳过: userId={}, permission={}", landlordId, permission);
            }
        }
    }

    @Override
    @Transactional
    public void revokePermissions(Long landlordId, List<String> permissions) {
        log.info("撤销房东权限: landlordId={}, permissions={}", landlordId, permissions);
        
        // 实际的权限撤销逻辑
        for (String permission : permissions) {
            // 查找要撤销的权限记录
            LambdaQueryWrapper<LandlordPermission> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(LandlordPermission::getUserId, landlordId)
                       .eq(LandlordPermission::getPermissionCode, permission)
                       .eq(LandlordPermission::getIsDeleted, 0);
            
            LandlordPermission existingPermission = landlordPermissionMapper.selectOne(queryWrapper);
            
            if (existingPermission != null) {
                // 软删除权限记录
                existingPermission.setIsDeleted(1);
                landlordPermissionMapper.updateById(existingPermission);
                log.info("成功撤销权限: userId={}, permission={}", landlordId, permission);
            } else {
                log.info("权限不存在，无需撤销: userId={}, permission={}", landlordId, permission);
            }
        }
    }

    @Override
    public List<String> getAvailablePermissions() {
        return new ArrayList<>(AVAILABLE_PERMISSIONS);
    }

    @Override
    @Transactional
    public void resetLandlordPermissions(Long landlordId) {
        log.info("重置房东权限: landlordId={}", landlordId);
        // TODO: 实际的权限重置逻辑
    }

    @Override
    @Transactional
    public void disableLandlord(Long landlordId, String reason) {
        UserInfo user = userInfoMapper.selectById(landlordId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        user.setStatus(BaseStatus.DISABLE);
        userInfoMapper.updateById(user);
        
        log.info("禁用房东账户: landlordId={}, reason={}", landlordId, reason);
    }

    @Override
    @Transactional
    public void enableLandlord(Long landlordId) {
        UserInfo user = userInfoMapper.selectById(landlordId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        user.setStatus(BaseStatus.ENABLE);
        userInfoMapper.updateById(user);
        
        log.info("启用房东账户: landlordId={}", landlordId);
    }
}