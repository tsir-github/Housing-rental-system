package com.atguigu.lease.web.app.mapper;

import com.atguigu.lease.model.entity.FeeValue;
import com.atguigu.lease.web.app.vo.fee.FeeValueVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * 杂费值Mapper接口
 */
public interface FeeValueMapper extends BaseMapper<FeeValue> {

    /**
     * 根据公寓ID查询杂费值列表
     * @param apartmentId 公寓ID
     * @return 杂费值列表
     */
    List<FeeValueVo> selectListByApartmentId(Long apartmentId);
}