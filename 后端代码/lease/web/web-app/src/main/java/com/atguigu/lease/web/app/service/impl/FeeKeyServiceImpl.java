package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.entity.FeeKey;
import com.atguigu.lease.web.app.mapper.FeeKeyMapper;
import com.atguigu.lease.web.app.service.FeeKeyService;
import com.atguigu.lease.web.app.vo.fee.FeeKeyVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 杂费键服务实现类
 */
@Service
@Slf4j
public class FeeKeyServiceImpl extends ServiceImpl<FeeKeyMapper, FeeKey> implements FeeKeyService {

    @Autowired
    private FeeKeyMapper feeKeyMapper;

    @Override
    public List<FeeKeyVo> listFeeInfo(Long landlordId) {
        return feeKeyMapper.selectFeeList(landlordId);
    }
}