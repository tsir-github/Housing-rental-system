package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.entity.FeeValue;
import com.atguigu.lease.web.app.mapper.FeeValueMapper;
import com.atguigu.lease.web.app.service.FeeValueService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 杂费值服务实现类
 */
@Service
@Slf4j
public class FeeValueServiceImpl extends ServiceImpl<FeeValueMapper, FeeValue> implements FeeValueService {
}