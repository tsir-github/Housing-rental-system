package com.atguigu.lease.web.app.service;

import com.atguigu.lease.model.entity.PaymentType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author liubo
* @description 针对表【payment_type(支付方式表)】的数据库操作Service
* @createDate 2023-07-26 11:12:39
*/
public interface PaymentTypeService extends IService<PaymentType> {
    List<PaymentType> listByRoomId(Long id);

    /**
     * 获取房东可选择的支付方式列表（包括官方发布的和房东发布的）
     * @param landlordId 房东ID
     * @return 可选择的支付方式列表
     */
    List<PaymentType> getAvailablePaymentTypesForLandlord(Long landlordId);
}
