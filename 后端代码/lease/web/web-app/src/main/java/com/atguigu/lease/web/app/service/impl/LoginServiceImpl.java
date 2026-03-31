package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.common.utils.CodeUtil;
import com.atguigu.lease.common.utils.JwtUtil;
import com.atguigu.lease.model.entity.UserInfo;
import com.atguigu.lease.model.entity.LandlordPermission;
import com.atguigu.lease.model.enums.BaseStatus;
import com.atguigu.lease.web.app.mapper.UserInfoMapper;
import com.atguigu.lease.web.app.mapper.LandlordPermissionMapper;
import com.atguigu.lease.web.app.service.LoginService;
import com.atguigu.lease.web.app.service.SmsService;
import com.atguigu.lease.web.app.vo.user.LoginVo;
import com.atguigu.lease.web.app.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private SmsService smsService;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UserInfoMapper userInfoMapper;
    
    @Autowired
    private LandlordPermissionMapper landlordPermissionMapper;

    @Override
    public void getCode(String phone) {
        String code = CodeUtil.getRandomCode(6);
        String key= RedisConstant.APP_LOGIN_PREFIX+phone;

        Boolean hasKey = redisTemplate.hasKey(key);
        if (hasKey){
            Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            if (RedisConstant.APP_LOGIN_CODE_TTL_SEC-ttl<RedisConstant.APP_LOGIN_CODE_RESEND_TIME_SEC){
                throw new LeaseException(ResultCodeEnum.APP_SEND_SMS_TOO_OFTEN);
            }
        }
        
        // 调试信息
        System.out.println("发送验证码调试 - 手机号: " + phone);
        System.out.println("发送验证码调试 - 生成的验证码: " + code);
        System.out.println("发送验证码调试 - Redis Key: " + key);
        
        try {
            smsService.sendCode(phone,code);
            System.out.println("发送验证码调试 - 短信发送成功");
        } catch (Exception e) {
            System.out.println("发送验证码调试 - 短信发送失败: " + e.getMessage());
            // 即使短信发送失败，也将验证码存储到Redis中，方便测试
        }
        
        redisTemplate.opsForValue().set(key,code,RedisConstant.APP_LOGIN_CODE_TTL_SEC, TimeUnit.SECONDS);
        System.out.println("发送验证码调试 - 验证码已存储到Redis，过期时间: " + RedisConstant.APP_LOGIN_CODE_TTL_SEC + "秒");
    }

    @Override
    public String login(LoginVo loginVo) {
        if (loginVo.getPhone()==null){
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_PHONE_EMPTY);
        }
        if (loginVo.getCode()==null){
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_EMPTY);
        }

        String key=RedisConstant.APP_LOGIN_PREFIX+loginVo.getPhone();
        System.out.println("登录调试 - Redis Key: " + key);
        
        String code = redisTemplate.opsForValue().get(key);
        System.out.println("登录调试 - Redis中的验证码: " + code);
        System.out.println("登录调试 - 用户输入的验证码: " + loginVo.getCode());
        
        if (code==null){
            System.out.println("登录调试 - 验证码为空，可能已过期");
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_EXPIRED);
        }
        if (!code.equals(loginVo.getCode())){
            System.out.println("登录调试 - 验证码不匹配");
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_ERROR);
        }

        System.out.println("登录调试 - 验证码验证通过");

        LambdaQueryWrapper<UserInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserInfo::getPhone,loginVo.getPhone());
        UserInfo userInfo = userInfoMapper.selectOne(queryWrapper);

        if(userInfo==null){
            //注册
            userInfo = new UserInfo();
            userInfo.setPhone(loginVo.getPhone());
            userInfo.setStatus(BaseStatus.ENABLE);
            userInfo.setNickname("用户-"+loginVo.getPhone().substring(7));
            // 设置用户类型，如果前端没有传递则默认为租客
            userInfo.setUserType(loginVo.getUserType() != null ? loginVo.getUserType() : 1);
            userInfoMapper.insert(userInfo);
            
            // 如果注册为房东，自动创建基础权限
            if (userInfo.getUserType() == 2) {
                createLandlordPermission(userInfo.getId());
            }
        }else {
            if (userInfo.getStatus()==BaseStatus.DISABLE){
                throw new LeaseException(ResultCodeEnum.APP_ACCOUNT_DISABLED_ERROR);
            }

        }

        return JwtUtil.createToken(userInfo.getId(),userInfo.getPhone());
    }

    @Override
    public UserInfoVo getUserInfoById(Long userId) {
        UserInfo userInfo = userInfoMapper.selectById(userId);
        
        UserInfoVo userInfoVo = new UserInfoVo();
        userInfoVo.setId(userInfo.getId()); // 设置用户ID
        userInfoVo.setNickname(userInfo.getNickname());
        userInfoVo.setAvatarUrl(userInfo.getAvatarUrl());
        
        // 直接使用数据库中的用户类型
        Integer userType = userInfo.getUserType() != null ? userInfo.getUserType() : 1;
        userInfoVo.setUserType(userType);
        userInfoVo.setStatus(userInfo.getStatus().getCode());
        
        // 判断是否为房东
        boolean isLandlord = (userType == 2);
        userInfoVo.setIsLandlord(isLandlord);
        
        // 如果是房东，获取权限列表
        if (isLandlord) {
            List<String> permissions = getLandlordPermissions(userId);
            userInfoVo.setPermissions(permissions);
            
            // 如果房东没有权限，自动创建基础权限
            if (permissions == null || permissions.isEmpty()) {
                createLandlordPermission(userId);
                // 重新获取权限
                permissions = getLandlordPermissions(userId);
                userInfoVo.setPermissions(permissions);
            }
        }

        return userInfoVo;
    }
    
    /**
     * 获取房东权限列表
     */
    private List<String> getLandlordPermissions(Long userId) {
        try {
            // 从数据库查询房东的真实权限
            return landlordPermissionMapper.selectPermissionsByUserId(userId);
        } catch (Exception e) {
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * 为新房东创建基础权限
     */
    private void createLandlordPermission(Long userId) {
        try {
            LandlordPermission permission = new LandlordPermission();
            permission.setUserId(userId);
            permission.setPermissionCode("LANDLORD_BASIC");
            permission.setGrantedAt(LocalDateTime.now());
            landlordPermissionMapper.insert(permission);
            
            System.out.println("房东权限创建成功 - 用户ID: " + userId);
        } catch (Exception e) {
            System.out.println("房东权限创建失败 - 用户ID: " + userId + ", 错误: " + e.getMessage());
        }
    }
}
