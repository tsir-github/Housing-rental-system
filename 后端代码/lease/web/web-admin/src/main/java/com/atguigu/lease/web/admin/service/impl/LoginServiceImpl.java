package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.common.utils.JwtUtil;
import com.atguigu.lease.model.entity.SystemUser;
import com.atguigu.lease.model.enums.BaseStatus;
import com.atguigu.lease.web.admin.mapper.SystemUserMapper;
import com.atguigu.lease.web.admin.service.LoginService;
import com.atguigu.lease.web.admin.vo.login.CaptchaVo;
import com.atguigu.lease.web.admin.vo.login.LoginVo;
import com.atguigu.lease.web.admin.vo.system.user.SystemUserInfoVo;
import com.atguigu.lease.web.admin.vo.system.user.SystemUserItemVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wf.captcha.SpecCaptcha;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private SystemUserMapper systemUserMapper;
    @Override
    public CaptchaVo getCaptcha() {
        //// 创建验证码生成器（130x48像素，4位验证码）
        SpecCaptcha specCaptcha = new SpecCaptcha(130, 48, 4);//图片宽高，验证码长度
        // 获取验证码文本并转为小写
        String code = specCaptcha.text().toLowerCase();
        // 生成Redis存储键（前缀 + UUID）
        String key= RedisConstant.ADMIN_LOGIN_PREFIX + UUID.randomUUID();
        // 将验证码存储到Redis，设置60秒有效期
        stringRedisTemplate.opsForValue().set(key,code, RedisConstant.ADMIN_LOGIN_CAPTCHA_TTL_SEC, TimeUnit.SECONDS);
        // 返回Base64格式的图片和Redis存储键
        return new  CaptchaVo(specCaptcha.toBase64(),key);
    }

    @Override
    public String login(LoginVo loginVo) {
        //判断`captchaCode`是否为空，若为空，则直接响应`验证码为空`；若不为空进行下一步判断。
        /* ================== 第一步：验证码校验 ================== */
        // 1.1 检查客户端是否提交验证码
        if (loginVo.getCaptchaCode()==null){
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_NOT_FOUND);// "未输入验证码"
        }
        // 1.2 从Redis获取验证码
        String code = stringRedisTemplate.opsForValue().get(loginVo.getCaptchaKey());
        if (code==null){
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_EXPIRED);// "验证码已过期"
        }
        // 1.3 比对验证码（不区分大小写）
        if (!code.equals(loginVo.getCaptchaCode().toLowerCase())){
            throw new LeaseException(ResultCodeEnum.ADMIN_CAPTCHA_CODE_ERROR); // "验证码错误"
        }
        /* ================== 第二步：用户状态校验 ================== */
        // 2.1 创建数据库查询条件
        /*LambdaQueryWrapper<SystemUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SystemUser::getUsername,loginVo.getUsername());*/
        // 2.2 查询用户信息
        SystemUser systemUser = systemUserMapper.selectOneByUsername(loginVo.getUsername());
        // 2.3 检查用户是否存在
        if (systemUser==null){
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_NOT_EXIST_ERROR);// "账号不存在"
        }
        // 2.4 检查账户是否被禁用
        if (systemUser.getStatus() != null && systemUser.getStatus().equals(BaseStatus.DISABLE.getCode())){
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_DISABLED_ERROR);// "账号被禁"
        }
        /* ================== 第三步：密码校验 ================== */
        // 3.1 将客户端密码进行MD5加密// 3.2 比对加密后的密码
        if (!systemUser.getPassword().equals(DigestUtils.md5Hex(loginVo.getPassword()))){
            throw new LeaseException(ResultCodeEnum.ADMIN_ACCOUNT_ERROR);// "用户名或密码错误
        }

        /* ================== 第四步：生成JWT ================== */

        return JwtUtil.createToken(systemUser.getId(), systemUser.getUsername());
    }

    @Override
    public SystemUserInfoVo getLoginUserInfoById(Long userId) {
        SystemUser systemUser = systemUserMapper.selectById(userId);
        SystemUserInfoVo systemUserInfoVo = new SystemUserInfoVo();
        systemUserInfoVo.setName(systemUser.getName());
        systemUserInfoVo.setAvatarUrl(systemUser.getAvatarUrl());
        return systemUserInfoVo;
    }
}
