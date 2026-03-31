package com.atguigu.lease.web.app.mapper;

import com.atguigu.lease.model.entity.ApartmentInfo;
import com.atguigu.lease.web.app.vo.apartment.ApartmentDetailVo;
import com.atguigu.lease.web.app.vo.apartment.ApartmentItemVo;
import com.atguigu.lease.web.app.vo.apartment.ApartmentQueryVo;
import com.atguigu.lease.web.app.vo.fee.FeeValueVo;
import com.atguigu.lease.web.app.vo.graph.GraphVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.util.List;

/**
* @author liubo
* @description 针对表【apartment_info(公寓信息表)】的数据库操作Mapper
* @createDate 2023-07-26 11:12:39
* @Entity com.atguigu.lease.model.entity.ApartmentInfo
*/
public interface ApartmentInfoMapper extends BaseMapper<ApartmentInfo> {

    /**
     * 房东分页查询公寓列表
     */
    IPage<ApartmentItemVo> pageItem(Page<ApartmentItemVo> page, ApartmentQueryVo queryVo);

    /**
     * 查询公寓详细信息（包含地区信息）
     * @param id 公寓ID
     * @return 公寓详细信息
     */
    ApartmentDetailVo selectDetailById(Long id);

    /**
     * 查询公寓关联的杂费信息
     * @param apartmentId 公寓ID
     * @return 杂费信息列表
     */
    List<FeeValueVo> selectFeeValuesByApartmentId(Long apartmentId);

    /**
     * 查询公寓关联的图片信息
     * @param apartmentId 公寓ID
     * @return 图片信息列表
     */
    List<GraphVo> selectImagesByApartmentId(Long apartmentId);
}




