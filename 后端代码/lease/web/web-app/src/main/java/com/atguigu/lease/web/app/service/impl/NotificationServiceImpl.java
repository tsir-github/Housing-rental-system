package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.entity.SystemUser;
import com.atguigu.lease.model.entity.UserInfo;
import com.atguigu.lease.web.app.mapper.SystemUserMapper;
import com.atguigu.lease.web.app.mapper.UserInfoMapper;

import com.atguigu.lease.web.app.service.NotificationService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 通知服务实现类
 */
@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private SystemUserMapper systemUserMapper;



    @Override
    public void notifyTenant(Long tenantId, String title, String message) {
        log.info("通知租客，租客ID: {}, 标题: {}, 消息: {}", tenantId, title, message);
        
        try {
            UserInfo tenant = userInfoMapper.selectById(tenantId);
            if (tenant != null) {
                // 发送推送通知
                sendPushNotification(tenantId, title, message);
                
                // 如果有手机号，发送短信
                if (tenant.getPhone() != null) {
                    sendSmsNotification(tenant.getPhone(), message);
                }
                
                // 邮件通知功能暂时禁用
                // if (tenant.getEmail() != null) {
                //     sendEmailNotification(tenant.getEmail(), title, message);
                // }
            }
        } catch (Exception e) {
            log.error("通知租客失败，租客ID: {}", tenantId, e);
        }
    }

    @Override
    public void notifyTenant(String phone, String title, String message) {
        log.info("通过手机号通知租客，手机号: {}, 标题: {}, 消息: {}", phone, title, message);
        
        try {
            // 根据手机号查找租客
            LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(UserInfo::getPhone, phone);
            UserInfo tenant = userInfoMapper.selectOne(queryWrapper);
            
            if (tenant != null) {
                notifyTenant(tenant.getId(), title, message);
            } else {
                // 如果找不到用户，直接发送短信
                sendSmsNotification(phone, message);
            }
        } catch (Exception e) {
            log.error("通过手机号通知租客失败，手机号: {}", phone, e);
        }
    }

    @Override
    public void notifyLandlord(Long landlordId, String title, String message) {
        log.info("通知房东，房东ID: {}, 标题: {}, 消息: {}", landlordId, title, message);
        
        try {
            UserInfo landlord = userInfoMapper.selectById(landlordId);
            if (landlord != null) {
                // 发送推送通知
                sendPushNotification(landlordId, title, message);
                
                // 如果有手机号，发送短信
                if (landlord.getPhone() != null) {
                    sendSmsNotification(landlord.getPhone(), message);
                }
                
                // 邮件通知功能暂时禁用
                // if (landlord.getEmail() != null) {
                //     sendEmailNotification(landlord.getEmail(), title, message);
                // }
            }
        } catch (Exception e) {
            log.error("通知房东失败，房东ID: {}", landlordId, e);
        }
    }

    @Override
    public void notifyOfficialStaff(Long staffId, String title, String message) {
        log.info("通知官方人员，人员ID: {}, 标题: {}, 消息: {}", staffId, title, message);
        
        try {
            if (staffId != null) {
                SystemUser staff = systemUserMapper.selectById(staffId);
                if (staff != null) {
                    // 发送短信通知
                    if (staff.getPhone() != null) {
                        sendSmsNotification(staff.getPhone(), message);
                    }
                }
            } else {
                // 通知所有官方人员
                batchNotify("OFFICIAL", title, message);
            }
        } catch (Exception e) {
            log.error("通知官方人员失败，人员ID: {}", staffId, e);
        }
    }

    @Override
    public void notifyReviewers(String title, String message) {
        log.info("通知审核员，标题: {}, 消息: {}", title, message);
        
        try {
            // 查询所有审核员（岗位ID为9的官方人员）
            LambdaQueryWrapper<SystemUser> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SystemUser::getPostId, 9L); // 官方线下审核员岗位ID
            List<SystemUser> reviewers = systemUserMapper.selectList(queryWrapper);
            
            for (SystemUser reviewer : reviewers) {
                // 邮件通知功能暂时禁用
                // if (reviewer.getEmail() != null) {
                //     sendEmailNotification(reviewer.getEmail(), title, message);
                // }
                
                // 发送短信通知
                if (reviewer.getPhone() != null) {
                    sendSmsNotification(reviewer.getPhone(), message);
                }
            }
        } catch (Exception e) {
            log.error("通知审核员失败", e);
        }
    }

    @Override
    public void sendSmsNotification(String phone, String message) {
        log.info("发送短信通知，手机号: {}, 消息: {}", phone, message);
        
        try {
            // 这里集成短信服务提供商的API
            // 比如阿里云短信、腾讯云短信等
            // 暂时只记录日志
            log.info("短信发送成功 - 手机号: {}, 内容: {}", phone, message);
            
            // 实际实现示例：
            // SmsClient smsClient = new SmsClient();
            // smsClient.sendSms(phone, message);
            
        } catch (Exception e) {
            log.error("发送短信失败，手机号: {}", phone, e);
        }
    }

    @Override
    public void sendEmailNotification(String email, String subject, String content) {
        log.info("发送邮件通知，邮箱: {}, 主题: {}", email, subject);
        
        try {
            // 这里集成邮件服务
            // 比如使用Spring Mail或第三方邮件服务
            // 暂时只记录日志
            log.info("邮件发送成功 - 邮箱: {}, 主题: {}, 内容: {}", email, subject, content);
            
            // 实际实现示例：
            // JavaMailSender mailSender;
            // SimpleMailMessage message = new SimpleMailMessage();
            // message.setTo(email);
            // message.setSubject(subject);
            // message.setText(content);
            // mailSender.send(message);
            
        } catch (Exception e) {
            log.error("发送邮件失败，邮箱: {}", email, e);
        }
    }

    @Override
    public void sendPushNotification(Long userId, String title, String message) {
        log.info("发送推送通知，用户ID: {}, 标题: {}, 消息: {}", userId, title, message);
        
        try {
            // 这里集成推送服务
            // 比如极光推送、友盟推送等
            // 暂时只记录日志
            log.info("推送通知发送成功 - 用户ID: {}, 标题: {}, 内容: {}", userId, title, message);
            
            // 实际实现示例：
            // PushService pushService = new PushService();
            // pushService.pushToUser(userId, title, message);
            
        } catch (Exception e) {
            log.error("发送推送通知失败，用户ID: {}", userId, e);
        }
    }

    @Override
    public void batchNotify(String userType, String title, String message) {
        log.info("批量通知，用户类型: {}, 标题: {}, 消息: {}", userType, title, message);
        
        try {
            switch (userType) {
                case "TENANT":
                    // 通知所有租客
                    notifyAllTenants(title, message);
                    break;
                case "LANDLORD":
                    // 通知所有房东
                    notifyAllLandlords(title, message);
                    break;
                case "OFFICIAL":
                    // 通知所有官方人员
                    notifyAllOfficialStaff(title, message);
                    break;
            }
        } catch (Exception e) {
            log.error("批量通知失败，用户类型: {}", userType, e);
        }
    }

    // ==================== 私有辅助方法 ====================

    private void notifyAllTenants(String title, String message) {
        // 查询所有租客并发送通知
        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getStatus, 1); // 正常状态的用户
        List<UserInfo> tenants = userInfoMapper.selectList(queryWrapper);
        
        for (UserInfo tenant : tenants) {
            sendPushNotification(tenant.getId(), title, message);
        }
    }

    private void notifyAllLandlords(String title, String message) {
        // 查询所有房东并发送通知
        // 这里需要根据实际的房东标识逻辑来查询
        // 暂时使用简单的逻辑
        log.info("批量通知所有房东: {}", message);
    }

    private void notifyAllOfficialStaff(String title, String message) {
        // 查询所有官方人员并发送通知
        List<SystemUser> staffList = systemUserMapper.selectList(null);
        
        for (SystemUser staff : staffList) {
            // 邮件通知功能暂时禁用
            // if (staff.getEmail() != null) {
            //     sendEmailNotification(staff.getEmail(), title, message);
            // }
        }
    }
}