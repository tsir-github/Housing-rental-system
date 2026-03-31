package com.atguigu.lease.web.app.service;

/**
 * 通知服务接口
 * 提供各种通知功能，包括短信、邮件、推送等
 */
public interface NotificationService {

    /**
     * 通知租客
     * @param tenantId 租客ID
     * @param title 通知标题
     * @param message 通知内容
     */
    void notifyTenant(Long tenantId, String title, String message);

    /**
     * 通过手机号通知租客
     * @param phone 手机号
     * @param title 通知标题
     * @param message 通知内容
     */
    void notifyTenant(String phone, String title, String message);

    /**
     * 通知房东
     * @param landlordId 房东ID
     * @param title 通知标题
     * @param message 通知内容
     */
    void notifyLandlord(Long landlordId, String title, String message);

    /**
     * 通知官方人员
     * @param staffId 人员ID，为null时通知所有官方人员
     * @param title 通知标题
     * @param message 通知内容
     */
    void notifyOfficialStaff(Long staffId, String title, String message);

    /**
     * 通知审核员
     * @param title 通知标题
     * @param message 通知内容
     */
    void notifyReviewers(String title, String message);

    /**
     * 发送短信通知
     * @param phone 手机号
     * @param message 短信内容
     */
    void sendSmsNotification(String phone, String message);

    /**
     * 发送邮件通知
     * @param email 邮箱地址
     * @param subject 邮件主题
     * @param content 邮件内容
     */
    void sendEmailNotification(String email, String subject, String content);

    /**
     * 发送推送通知
     * @param userId 用户ID
     * @param title 推送标题
     * @param message 推送内容
     */
    void sendPushNotification(Long userId, String title, String message);

    /**
     * 批量通知
     * @param userType 用户类型 (TENANT/LANDLORD/OFFICIAL)
     * @param title 通知标题
     * @param message 通知内容
     */
    void batchNotify(String userType, String title, String message);
}