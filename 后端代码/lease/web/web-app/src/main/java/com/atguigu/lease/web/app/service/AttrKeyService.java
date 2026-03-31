package com.atguigu.lease.web.app.service;

import com.atguigu.lease.model.entity.AttrKey;
import com.atguigu.lease.web.app.vo.attr.AttrKeyVo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author liubo
* @description 针对表【attr_key(房间基本属性表)】的数据库操作Service
* @createDate 2023-07-26 11:12:39
*/
public interface AttrKeyService extends IService<AttrKey> {

    /**
     * 查询全部属性名称和属性值列表
     */
    List<AttrKeyVo> listAttrInfo();

    /**
     * 获取房东可选择的属性列表（包括官方发布的和房东发布的）
     * @param landlordId 房东ID
     * @return 可选择的属性列表
     */
    List<AttrKeyVo> getAvailableAttributesForLandlord(Long landlordId);

    /**
     * 房东创建自定义属性
     * @param request 自定义属性请求
     * @return 创建的属性信息
     */
    AttrKeyVo createCustomAttribute(com.atguigu.lease.web.app.vo.landlord.CustomAttributeRequest request);
}
