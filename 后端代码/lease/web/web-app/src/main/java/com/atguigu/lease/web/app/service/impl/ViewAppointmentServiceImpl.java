package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.entity.ViewAppointment;
import com.atguigu.lease.model.entity.RoomInfo;
import com.atguigu.lease.model.entity.UserInfo;
import com.atguigu.lease.model.enums.AppointmentStatus;
import com.atguigu.lease.model.enums.GuideType;
import com.atguigu.lease.model.enums.PublisherType;
import com.atguigu.lease.web.app.mapper.ViewAppointmentMapper;
import com.atguigu.lease.web.app.service.ApartmentInfoService;
import com.atguigu.lease.web.app.service.ViewAppointmentService;
import com.atguigu.lease.web.app.service.RoomInfoService;
import com.atguigu.lease.web.app.service.UserInfoService;
import com.atguigu.lease.web.app.vo.apartment.ApartmentItemVo;
import com.atguigu.lease.web.app.vo.appointment.AppointmentDetailVo;
import com.atguigu.lease.web.app.vo.appointment.AppointmentItemVo;
import com.atguigu.lease.web.app.vo.room.RoomAvailabilityVo;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * @author liubo
 * @description 针对表【view_appointment(预约看房信息表)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Slf4j
@Service
public class ViewAppointmentServiceImpl extends ServiceImpl<ViewAppointmentMapper, ViewAppointment>
        implements ViewAppointmentService {

    @Autowired
    private ViewAppointmentMapper viewAppointmentMapper;

    @Autowired
    private ApartmentInfoService apartmentInfoService;

    @Autowired
    private RoomInfoService roomInfoService;

    @Autowired
    private UserInfoService userInfoService;
    
    @Override
    public List<AppointmentItemVo> listItemByUserId(Long userId) {
        return viewAppointmentMapper.listItemByUserId(userId);
    }

    @Override
    public AppointmentDetailVo getDetailById(Long id) {
        ViewAppointment viewAppointment = viewAppointmentMapper.selectById(id);
        if (viewAppointment == null) {
            throw new RuntimeException("预约不存在");
        }
        
        AppointmentDetailVo appointmentDetailVo = new AppointmentDetailVo();
        BeanUtils.copyProperties(viewAppointment, appointmentDetailVo);

        // 获取公寓信息
        ApartmentItemVo apartmentItemVo = apartmentInfoService.selectApartmentItemVoById(viewAppointment.getApartmentId());
        appointmentDetailVo.setApartmentItemVo(apartmentItemVo);
        
        // 获取房间详细信息
        RoomInfo roomInfo = roomInfoService.getById(viewAppointment.getRoomId());
        appointmentDetailVo.setRoomInfo(roomInfo);
        
        // 获取租客用户信息
        UserInfo userInfo = userInfoService.getById(viewAppointment.getUserId());
        appointmentDetailVo.setUserInfo(userInfo);

        return appointmentDetailVo;
    }

    @Override
    @Transactional
    public Long createAppointmentWithGuide(ViewAppointment appointment) {
        // 根据房源发布者类型决定预约路由逻辑
        RoomInfo roomInfo = roomInfoService.getById(appointment.getRoomId());
        if (roomInfo == null) {
            throw new RuntimeException("房间不存在");
        }

        // 根据发布者类型设置预约处理逻辑（不再使用带看人概念）
        if (PublisherType.LANDLORD.equals(roomInfo.getPublisherType())) {
            // 房东发布的房源：预约发送到房东模块 + 官方模块
            appointment.setGuideType(GuideType.LANDLORD);
            appointment.setGuideId(roomInfo.getPublisherId());
            log.info("房东发布的房源，预约将发送给房东ID: {}，同时发送到官方预约管理", roomInfo.getPublisherId());
        } else {
            // 官方发布的房源：预约只发送到官方模块
            appointment.setGuideType(GuideType.OFFICIAL);
            // 使用默认的官方处理人员ID
            appointment.setGuideId(getDefaultOfficialGuideId());
            log.info("官方发布的房源，预约将发送给官方预约管理");
        }

        // 设置初始状态
        appointment.setAppointmentStatus(AppointmentStatus.WAITING);
        
        // 保存预约
        this.save(appointment);
        
        // 发送通知给对应的处理模块
        sendAppointmentNotification(appointment);
        
        return appointment.getId();
    }

    /**
     * 获取默认官方带看人员ID
     */
    private Long getDefaultOfficialGuideId() {
        // 这里可以从配置或数据库获取默认官方人员ID
        // 暂时使用固定值，后续可以优化为动态获取
        return 1L;
    }

    @Override
    public List<AppointmentItemVo> listByGuideTypeAndId(Integer guideType, Long guideId) {
        return viewAppointmentMapper.listByGuideTypeAndId(guideType, guideId);
    }

    @Override
    public List<AppointmentItemVo> listByRoomId(Long roomId) {
        return viewAppointmentMapper.listByRoomId(roomId);
    }

    @Override
    @Transactional
    public void processAppointment(Long appointmentId, Integer status, String remarks) {
        LambdaUpdateWrapper<ViewAppointment> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ViewAppointment::getId, appointmentId)
                    .set(ViewAppointment::getAppointmentStatus, AppointmentStatus.fromCode(status))
                    .set(ViewAppointment::getAdditionalInfo, remarks);
        
        this.update(updateWrapper);
        
        // 发送状态更新通知
        ViewAppointment appointment = this.getById(appointmentId);
        sendStatusUpdateNotification(appointment, status);
    }

    @Override
    public List<AppointmentItemVo> listOfficialAppointments() {
        return viewAppointmentMapper.listByGuideType(GuideType.OFFICIAL.getCode());
    }

    @Override
    public List<AppointmentItemVo> listLandlordAppointments(Long landlordId) {
        return viewAppointmentMapper.selectLandlordAppointmentsList(landlordId, null);
    }

    @Override
    public List<Map<String, Object>> getGuideOptionsByRoomId(Long roomId) {
        List<Map<String, Object>> options = new ArrayList<>();
        
        // 获取房间信息
        RoomInfo roomInfo = roomInfoService.getById(roomId);
        if (roomInfo == null) {
            throw new RuntimeException("房间不存在");
        }

        // 现在不需要租客选择，直接返回房源类型信息供前端参考
        Map<String, Object> info = new HashMap<>();
        if (PublisherType.LANDLORD.equals(roomInfo.getPublisherType())) {
            info.put("publisherType", "landlord");
            info.put("description", "房东发布的房源，将由房东带看");
            info.put("guideType", GuideType.LANDLORD.getCode());
            info.put("landlordId", roomInfo.getPublisherId());
        } else {
            info.put("publisherType", "official");
            info.put("description", "官方发布的房源，将由官方人员带看");
            info.put("guideType", GuideType.OFFICIAL.getCode());
            info.put("officialId", roomInfo.getPublisherId() != null ? roomInfo.getPublisherId() : getDefaultOfficialGuideId());
        }
        options.add(info);

        return options;
    }

    @Override
    public AppointmentDetailVo getLandlordAppointmentDetail(Long appointmentId, Long landlordId) {
        // 由于列表接口已经确保了权限控制，这里可以直接获取详情
        // 如果需要额外的安全保障，可以在SQL层面添加权限验证
        ViewAppointment appointment = this.getById(appointmentId);
        
        if (appointment == null) {
            throw new RuntimeException("预约不存在");
        }
        
        // 可选：添加简单的权限检查（推荐保留这个检查作为安全保障）
        if (!appointment.getGuideId().equals(landlordId) || !GuideType.LANDLORD.equals(appointment.getGuideType())) {
            throw new RuntimeException("无权查看此预约详情");
        }
        
        // 获取预约详情
        return this.getDetailById(appointmentId);
    }

    /**
     * 发送预约通知
     */
    private void sendAppointmentNotification(ViewAppointment appointment) {
        try {
            // 这里可以实现具体的通知逻辑
            // 例如：短信、邮件、站内消息等
            log.info("发送预约通知：预约ID={}, 带看人类型={}, 带看人ID={}", 
                    appointment.getId(), appointment.getGuideType(), appointment.getGuideId());
        } catch (Exception e) {
            log.error("发送预约通知失败", e);
        }
    }

    /**
     * 发送状态更新通知
     */
    private void sendStatusUpdateNotification(ViewAppointment appointment, Integer status) {
        try {
            log.info("发送状态更新通知：预约ID={}, 新状态={}", appointment.getId(), status);
        } catch (Exception e) {
            log.error("发送状态更新通知失败", e);
        }
    }

    @Override
    public RoomAvailabilityVo checkRoomAvailabilityForAppointment(Long roomId) {
        log.info("检查房源预约可用性 - roomId: {}", roomId);
        
        try {
            // 1. 检查房源是否存在
            RoomInfo roomInfo = roomInfoService.getById(roomId);
            if (roomInfo == null) {
                log.warn("房源不存在 - roomId: {}", roomId);
                return RoomAvailabilityVo.notFound();
            }
            
            // 2. 检查房源是否已发布（房东意愿）
            if (roomInfo.getIsRelease() == null || !com.atguigu.lease.model.enums.ReleaseStatus.RELEASED.equals(roomInfo.getIsRelease())) {
                log.warn("房源未发布 - roomId: {}", roomId);
                return RoomAvailabilityVo.notReleased();
            }
            
            // 3. 检查审核状态
            if (!com.atguigu.lease.model.enums.ReviewStatus.APPROVED.equals(roomInfo.getReviewStatus())) {
                log.warn("房源未通过审核 - roomId: {}", roomId);
                return RoomAvailabilityVo.notApproved();
            }
            
            // 4. 对于预约看房，即使房源已被占用，也可以预约（但会有特殊提示）
            // 检查是否有占用房源的租约
            // 这里可以调用其他服务来检查租约状态
            // 为了简化，我们复用DirectMoveInApplicationService的逻辑
            
            log.info("房源可预约看房 - roomId: {}", roomId);
            return RoomAvailabilityVo.available();
            
        } catch (Exception e) {
            log.error("检查房源预约可用性失败 - roomId: {}, error: {}", roomId, e.getMessage(), e);
            return new RoomAvailabilityVo(false, "ERROR", "系统错误", "检查房源可用性时发生错误，请稍后重试");
        }
    }
}




