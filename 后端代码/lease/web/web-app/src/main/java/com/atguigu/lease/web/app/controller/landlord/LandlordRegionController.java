package com.atguigu.lease.web.app.controller.landlord;

import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.model.entity.CityInfo;
import com.atguigu.lease.model.entity.DistrictInfo;
import com.atguigu.lease.model.entity.ProvinceInfo;
import com.atguigu.lease.web.app.service.CityInfoService;
import com.atguigu.lease.web.app.service.DistrictInfoService;
import com.atguigu.lease.web.app.service.ProvinceInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 房东地区信息管理控制器
 */
@Tag(name = "房东地区信息管理")
@RestController
@RequestMapping("/app/landlord/region")
@Slf4j
public class LandlordRegionController {

    @Autowired
    private ProvinceInfoService provinceInfoService;
    
    @Autowired
    private CityInfoService cityInfoService;
    
    @Autowired
    private DistrictInfoService districtInfoService;

    @Operation(summary = "房东查询省份信息列表")
    @GetMapping("/province/list")
    public Result<List<ProvinceInfo>> listProvince() {
        try {
            LambdaQueryWrapper<ProvinceInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ProvinceInfo::getIsDeleted, 0);
            queryWrapper.orderByAsc(ProvinceInfo::getId);
            
            List<ProvinceInfo> list = provinceInfoService.list(queryWrapper);
            return Result.ok(list);
        } catch (Exception e) {
            log.error("查询省份信息列表失败", e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东根据省份id查询城市信息列表")
    @GetMapping("/city/listByProvinceId")
    public Result<List<CityInfo>> listCityByProvinceId(@RequestParam Long id) {
        try {
            LambdaQueryWrapper<CityInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CityInfo::getProvinceId, id);
            queryWrapper.eq(CityInfo::getIsDeleted, 0);
            queryWrapper.orderByAsc(CityInfo::getId);
            
            List<CityInfo> list = cityInfoService.list(queryWrapper);
            return Result.ok(list);
        } catch (Exception e) {
            log.error("根据省份ID查询城市信息列表失败", e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东根据城市id查询区县信息列表")
    @GetMapping("/district/listByCityId")
    public Result<List<DistrictInfo>> listDistrictByCityId(@RequestParam Long id) {
        try {
            LambdaQueryWrapper<DistrictInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DistrictInfo::getCityId, id);
            queryWrapper.eq(DistrictInfo::getIsDeleted, 0);
            queryWrapper.orderByAsc(DistrictInfo::getId);
            
            List<DistrictInfo> list = districtInfoService.list(queryWrapper);
            return Result.ok(list);
        } catch (Exception e) {
            log.error("根据城市ID查询区县信息列表失败", e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东查询所有城市信息列表")
    @GetMapping("/city/list")
    public Result<List<CityInfo>> listCity() {
        try {
            LambdaQueryWrapper<CityInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CityInfo::getIsDeleted, 0);
            queryWrapper.orderByAsc(CityInfo::getId);
            
            List<CityInfo> list = cityInfoService.list(queryWrapper);
            return Result.ok(list);
        } catch (Exception e) {
            log.error("查询城市信息列表失败", e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }

    @Operation(summary = "房东查询所有区县信息列表")
    @GetMapping("/district/list")
    public Result<List<DistrictInfo>> listDistrict() {
        try {
            LambdaQueryWrapper<DistrictInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DistrictInfo::getIsDeleted, 0);
            queryWrapper.orderByAsc(DistrictInfo::getId);
            
            List<DistrictInfo> list = districtInfoService.list(queryWrapper);
            return Result.ok(list);
        } catch (Exception e) {
            log.error("查询区县信息列表失败", e);
            return Result.fail("查询失败：" + e.getMessage());
        }
    }
}