package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.entity.ApartmentInfo;
import com.atguigu.lease.common.login.LoginUser;
import com.atguigu.lease.model.entity.RoomInfo;
import com.atguigu.lease.model.enums.BaseStatus;
import com.atguigu.lease.web.app.mapper.ApartmentInfoMapper;
import com.atguigu.lease.web.app.mapper.DirectMoveInApplicationMapper;
import com.atguigu.lease.web.app.mapper.LandlordPermissionMapper;
import com.atguigu.lease.web.app.mapper.RoomInfoMapper;
import com.atguigu.lease.web.app.mapper.ViewAppointmentMapper;
import com.atguigu.lease.web.app.service.LandlordManagementService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 房东管理服务实现类
 */
@Service
@Slf4j
public class LandlordManagementServiceImpl implements LandlordManagementService {

    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;
    
    @Autowired
    private RoomInfoMapper roomInfoMapper;
    
    @Autowired
    private DirectMoveInApplicationMapper directMoveInApplicationMapper;
    
    @Autowired
    private LandlordPermissionMapper landlordPermissionMapper;
    
    @Autowired
    private ViewAppointmentMapper viewAppointmentMapper;

    // 房东基础权限列表
    private static final Set<String> LANDLORD_BASIC_PERMISSIONS = Set.of(
        "apartment:view",      // 查看公寓
        "apartment:create",    // 创建公寓
        "apartment:update",    // 更新公寓
        "apartment:delete",    // 删除公寓
        "room:view",          // 查看房间
        "room:create",        // 创建房间
        "room:update",        // 更新房间
        "room:delete",        // 删除房间
        "facility:view",      // 查看配套
        "facility:create",    // 创建配套
        "facility:update",    // 更新配套
        "facility:delete",    // 删除配套
        "label:view",         // 查看标签
        "label:create",       // 创建标签
        "label:update",       // 更新标签
        "label:delete",       // 删除标签
        "application:view",   // 查看申请
        "application:process", // 处理申请
        "appointment:view",   // 查看预约
        "appointment:process", // 处理预约
        "statistics:view"     // 查看统计
    );

    @Override
    public boolean hasPermission(Long landlordId, String permission) {
        try {
            // 基础权限验证
            if (!LANDLORD_BASIC_PERMISSIONS.contains(permission)) {
                log.warn("房东权限验证失败：未知权限, landlordId: {}, permission: {}", landlordId, permission);
                return false;
            }

            // TODO: 这里可以扩展为从数据库查询具体权限
            // 目前所有房东都有基础权限
            return true;
            
        } catch (Exception e) {
            log.error("房东权限验证异常: landlordId={}, permission={}", landlordId, permission, e);
            return false;
        }
    }

    @Override
    public boolean hasResourcePermission(Long landlordId, String resourceType, Long resourceId) {
        try {
            switch (resourceType.toLowerCase()) {
                case "apartment":
                    return isApartmentOwner(landlordId, resourceId);
                case "room":
                    return isRoomOwner(landlordId, resourceId);
                default:
                    log.warn("未知资源类型: {}", resourceType);
                    return false;
            }
        } catch (Exception e) {
            log.error("资源权限验证异常: landlordId={}, resourceType={}, resourceId={}", 
                landlordId, resourceType, resourceId, e);
            return false;
        }
    }

    @Override
    public List<String> getLandlordPermissions(Long landlordId) {
        try {
            // 从数据库查询房东的真实权限
            List<String> permissions = landlordPermissionMapper.selectPermissionsByUserId(landlordId);
            
            if (permissions == null || permissions.isEmpty()) {
                log.warn("房东无权限记录: landlordId={}", landlordId);
                return new ArrayList<>();
            }
            
            log.info("获取房东权限成功: landlordId={}, permissions={}", landlordId, permissions);
            return permissions;
            
        } catch (Exception e) {
            log.error("获取房东权限失败: landlordId={}", landlordId, e);
            return new ArrayList<>();
        }
    }

    @Override
    public boolean isLandlord(LoginUser loginUser) {
        if (loginUser == null) {
            return false;
        }
        
        // 验证用户状态
        if (!BaseStatus.ENABLE.equals(loginUser.getStatus())) {
            return false;
        }
        
        // 检查用户是否在landlord_permission表中有权限记录
        try {
            List<String> permissions = getLandlordPermissions(loginUser.getUserId());
            return permissions != null && !permissions.isEmpty();
        } catch (Exception e) {
            log.error("检查房东身份失败: userId={}", loginUser.getUserId(), e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getLandlordStatistics(Long landlordId) {
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            // 统计公寓数量
            LambdaQueryWrapper<ApartmentInfo> apartmentWrapper = new LambdaQueryWrapper<>();
            apartmentWrapper.eq(ApartmentInfo::getLandlordId, landlordId);
            apartmentWrapper.eq(ApartmentInfo::getIsDeleted, 0);
            Long apartmentCount = apartmentInfoMapper.selectCount(apartmentWrapper);
            
            // 统计房间数量
            LambdaQueryWrapper<RoomInfo> roomWrapper = new LambdaQueryWrapper<>();
            roomWrapper.eq(RoomInfo::getPublisherId, landlordId);
            roomWrapper.eq(RoomInfo::getPublisherType, 2); // 房东发布
            roomWrapper.eq(RoomInfo::getIsDeleted, 0);
            Long roomCount = roomInfoMapper.selectCount(roomWrapper);
            
            // 统计已发布房间数量
            LambdaQueryWrapper<RoomInfo> publishedRoomWrapper = new LambdaQueryWrapper<>();
            publishedRoomWrapper.eq(RoomInfo::getPublisherId, landlordId);
            publishedRoomWrapper.eq(RoomInfo::getPublisherType, 2);
            publishedRoomWrapper.eq(RoomInfo::getIsRelease, 1);
            publishedRoomWrapper.eq(RoomInfo::getIsDeleted, 0);
            Long publishedRoomCount = roomInfoMapper.selectCount(publishedRoomWrapper);
            
            // 统计待处理申请数量
            Long pendingApplicationCount = directMoveInApplicationMapper.countLandlordApplications(landlordId);
            
            // 计算出租率
            Double occupancyRate = roomCount > 0 ? (publishedRoomCount * 100.0 / roomCount) : 0.0;
            
            // 统计待确认预约数量（假设状态1为待确认）
            Long pendingAppointmentCount = viewAppointmentMapper.countLandlordAppointmentsByStatus(landlordId, 1);
            
            // 统计今日预约数量
            Long todayAppointmentCount = viewAppointmentMapper.countLandlordTodayAppointments(landlordId);
            
            statistics.put("apartmentCount", apartmentCount);
            statistics.put("roomCount", roomCount);
            statistics.put("publishedRoomCount", publishedRoomCount);
            statistics.put("availableRoomCount", publishedRoomCount); // 简化处理，已发布即可租
            statistics.put("occupiedRoomCount", roomCount - publishedRoomCount); // 简化处理
            statistics.put("pendingApplicationCount", pendingApplicationCount);
            statistics.put("pendingAppointmentCount", pendingAppointmentCount);
            statistics.put("todayAppointmentCount", todayAppointmentCount);
            statistics.put("occupancyRate", occupancyRate);
            statistics.put("monthlyNewRooms", 0L); // TODO: 实现月度统计
            statistics.put("monthlyDeals", 0L); // TODO: 实现月度成交统计
            statistics.put("monthlyIncome", 0.0); // TODO: 实现月度收入统计
            
            log.info("获取房东统计信息成功: landlordId={}, statistics={}", landlordId, statistics);
            
        } catch (Exception e) {
            log.error("获取房东统计信息失败: landlordId={}", landlordId, e);
            // 返回默认值
            statistics.put("apartmentCount", 0L);
            statistics.put("roomCount", 0L);
            statistics.put("publishedRoomCount", 0L);
            statistics.put("availableRoomCount", 0L);
            statistics.put("occupiedRoomCount", 0L);
            statistics.put("pendingApplicationCount", 0L);
            statistics.put("pendingAppointmentCount", 0L);
            statistics.put("todayAppointmentCount", 0L);
            statistics.put("occupancyRate", 0.0);
            statistics.put("monthlyNewRooms", 0L);
            statistics.put("monthlyDeals", 0L);
            statistics.put("monthlyIncome", 0.0);
        }
        
        return statistics;
    }

    /**
     * 验证是否为公寓所有者
     */
    private boolean isApartmentOwner(Long landlordId, Long apartmentId) {
        LambdaQueryWrapper<ApartmentInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApartmentInfo::getId, apartmentId);
        wrapper.eq(ApartmentInfo::getLandlordId, landlordId);
        wrapper.eq(ApartmentInfo::getIsDeleted, 0);
        
        return apartmentInfoMapper.selectCount(wrapper) > 0;
    }

    /**
     * 验证是否为房间所有者
     */
    private boolean isRoomOwner(Long landlordId, Long roomId) {
        LambdaQueryWrapper<RoomInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RoomInfo::getId, roomId);
        wrapper.eq(RoomInfo::getPublisherId, landlordId);
        wrapper.eq(RoomInfo::getPublisherType, 2); // 房东发布
        wrapper.eq(RoomInfo::getIsDeleted, 0);
        
        return roomInfoMapper.selectCount(wrapper) > 0;
    }
}