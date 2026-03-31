package com.atguigu.lease.web.app.service;

import com.atguigu.lease.model.entity.FeeKey;
import com.atguigu.lease.web.app.vo.fee.FeeKeyVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 杂费键服务接口
 */
public interface FeeKeyService extends IService<FeeKey> {

    /**
     * 查询杂费键和杂费值列表
     * @param landlordId 房东ID（可选）
     * @return 杂费键列表（包含杂费值）
     */
    List<FeeKeyVo> listFeeInfo(Long landlordId);
}