package com.atguigu.lease.web.app.mapper;

import com.atguigu.lease.model.entity.FeeKey;
import com.atguigu.lease.web.app.vo.fee.FeeKeyVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * 杂费键Mapper接口
 */
public interface FeeKeyMapper extends BaseMapper<FeeKey> {

    /**
     * 查询杂费键和杂费值列表
     * @param landlordId 房东ID（可选）
     * @return 杂费键列表（包含杂费值）
     */
    List<FeeKeyVo> selectFeeList(Long landlordId);
}