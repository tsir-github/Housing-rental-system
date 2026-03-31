package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.web.app.mapper.*;
import com.atguigu.lease.web.app.service.KeywordExtractionService;
import com.atguigu.lease.web.app.vo.ai.KeywordExtractionResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 关键词提取Service实现类
 * 核心设计: 应用启动时从数据库加载所有关键词映射到内存缓存
 * 提取时直接查询缓存,避免重复数据库查询
 * 
 * 支持四大类关键词:
 * 1. 地址: 省份、城市、地区 (province_info, city_info, district_info)
 * 2. 房间属性: 房型、朝向、面积等 (attr_value)
 * 3. 房间配套: 空调、洗衣机等 (facility_info where type=2)
 * 4. 房间标签: 朝南、独卫等 (label_info where type=2)
 */
@Slf4j
@Service
public class KeywordExtractionServiceImpl implements KeywordExtractionService {

    @Autowired(required = false)
    private ProvinceInfoMapper provinceInfoMapper;

    @Autowired(required = false)
    private CityInfoMapper cityInfoMapper;

    @Autowired(required = false)
    private DistrictInfoMapper districtInfoMapper;

    @Autowired(required = false)
    private AttrValueMapper attrValueMapper;

    @Autowired(required = false)
    private FacilityInfoMapper facilityInfoMapper;

    @Autowired(required = false)
    private LabelInfoMapper labelInfoMapper;

    // ==================== 缓存映射 ====================
    
    /**
     * 省份名称 -> 省份ID 映射
     */
    private Map<String, Long> provinceNameToIdMap = new HashMap<>();

    /**
     * 城市名称 -> 城市ID 映射
     */
    private Map<String, Long> cityNameToIdMap = new HashMap<>();

    /**
     * 地区名称 -> 地区ID 映射
     */
    private Map<String, Long> districtNameToIdMap = new HashMap<>();

    /**
     * 房间属性值名称 -> 属性值ID 映射
     * 例: "两室" -> 8, "朝南" -> 5
     */
    private Map<String, Long> attrValueNameToIdMap = new HashMap<>();

    /**
     * 房间配套名称 -> 配套ID 映射 (type=2)
     * 例: "空调" -> 28, "洗衣机" -> 29
     */
    private Map<String, Long> facilityNameToIdMap = new HashMap<>();

    /**
     * 房间标签名称 -> 标签ID 映射 (type=2)
     * 例: "朝南" -> 5, "独卫" -> 15
     */
    private Map<String, Long> labelNameToIdMap = new HashMap<>();

    // ==================== 正则表达式 ====================

    private static final Pattern PRICE_PATTERN = Pattern.compile(
        "(\\d+)\\s*[到-]\\s*(\\d+)|预算\\s*(\\d+)|价格\\s*(\\d+)|租金\\s*(\\d+)|(\\d+)\\s*元|最高\\s*(\\d+)|最低\\s*(\\d+)"
    );

    private static final Pattern DATE_PATTERN = Pattern.compile(
        "(\\d{4})[-/](\\d{1,2})[-/](\\d{1,2})|(\\d{1,2})月(\\d{1,2})日|今天|明天|后天|下周|下月|下半年|年底"
    );

    private static final Pattern LEASE_DURATION_PATTERN = Pattern.compile(
        "(\\d+)\\s*个?月|半年|一年|两年|三年|长期|短期"
    );

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 应用启动时初始化所有关键词缓存
     * 这样做的好处:
     * 1. 避免每次提取关键词都查询数据库
     * 2. 内存查询速度快
     * 3. 支持模糊匹配和精确匹配
     */
    @PostConstruct
    public void initKeywordCache() {
        log.info("开始初始化关键词缓存...");
        try {
            // 加载省份信息
            if (provinceInfoMapper != null) {
                List<ProvinceInfo> provinces = provinceInfoMapper.selectList(null);
                provinces.forEach(p -> provinceNameToIdMap.put(p.getName(), p.getId()));
                log.info("加载了 {} 个省份", provinces.size());
            }

            // 加载城市信息
            if (cityInfoMapper != null) {
                List<CityInfo> cities = cityInfoMapper.selectList(null);
                cities.forEach(c -> cityNameToIdMap.put(c.getName(), c.getId()));
                log.info("加载了 {} 个城市", cities.size());
            }

            // 加载地区信息
            if (districtInfoMapper != null) {
                List<DistrictInfo> districts = districtInfoMapper.selectList(null);
                districts.forEach(d -> districtNameToIdMap.put(d.getName(), d.getId()));
                log.info("加载了 {} 个地区", districts.size());
            }

            // 加载房间属性值 (所有属性值,包括房型、朝向、面积等)
            if (attrValueMapper != null) {
                List<AttrValue> attrValues = attrValueMapper.selectList(null);
                attrValues.forEach(av -> attrValueNameToIdMap.put(av.getName(), av.getId()));
                log.info("加载了 {} 个房间属性值", attrValues.size());
            }

            // 加载房间配套 (type=2 表示房间专属配套)
            if (facilityInfoMapper != null) {
                LambdaQueryWrapper<FacilityInfo> facilityWrapper = new LambdaQueryWrapper<>();
                facilityWrapper.eq(FacilityInfo::getType, 2);
                List<FacilityInfo> facilities = facilityInfoMapper.selectList(facilityWrapper);
                facilities.forEach(f -> facilityNameToIdMap.put(f.getName(), f.getId()));
                log.info("加载了 {} 个房间配套", facilities.size());
            }

            // 加载房间标签 (type=2 表示房间专属标签)
            if (labelInfoMapper != null) {
                LambdaQueryWrapper<LabelInfo> labelWrapper = new LambdaQueryWrapper<>();
                labelWrapper.eq(LabelInfo::getType, 2);
                List<LabelInfo> labels = labelInfoMapper.selectList(labelWrapper);
                labels.forEach(l -> labelNameToIdMap.put(l.getName(), l.getId()));
                log.info("加载了 {} 个房间标签", labels.size());
            }

            log.info("关键词缓存初始化完成!");
        } catch (Exception e) {
            log.error("初始化关键词缓存失败", e);
        }
    }

    /**
     * 从用户消息中提取关键词
     */
    @Override
    public KeywordExtractionResult extractKeywords(String userMessage) {
        log.info("开始提取关键词，消息: {}", userMessage);

        KeywordExtractionResult result = new KeywordExtractionResult();
        result.setRawText(userMessage);

        // 1. 提取地址信息 (省市区)
        extractLocationKeywords(userMessage, result);

        // 2. 提取房间属性 (通过attr_value表)
        extractAttrKeywords(userMessage, result);

        // 3. 提取房间配套 (facility_info.type=2)
        extractFacilityKeywords(userMessage, result);

        // 4. 提取房间标签 (label_info.type=2)
        extractLabelKeywords(userMessage, result);

        // 5. 提取价格范围
        KeywordExtractionResult.PriceRange priceRange = extractPriceRange(userMessage);
        result.setPriceRange(priceRange);

        // 6. 提取发布者类型
        String publisherType = extractPublisherType(userMessage);
        result.setPublisherType(publisherType);

        // 7. 提取入住时间
        LocalDate moveInDate = extractMoveInDate(userMessage);
        result.setMoveInDate(moveInDate);

        // 8. 提取租期
        Integer leaseDuration = extractLeaseDuration(userMessage);
        result.setLeaseDuration(leaseDuration);

        // 9. 计算置信度
        Double confidence = calculateConfidence(result);
        result.setConfidence(confidence);

        log.info("关键词提取完成，置信度: {}", confidence);
        return result;
    }

    /**
     * 提取地址关键词 (省市区)
     */
    private void extractLocationKeywords(String message, KeywordExtractionResult result) {
        List<Long> provinceIds = new ArrayList<>();
        List<Long> cityIds = new ArrayList<>();
        List<Long> districtIds = new ArrayList<>();
        StringBuilder addressDetail = new StringBuilder();

        // 查询省份
        for (String provinceName : provinceNameToIdMap.keySet()) {
            if (message.contains(provinceName)) {
                provinceIds.add(provinceNameToIdMap.get(provinceName));
                addressDetail.append(provinceName);
            }
        }

        // 查询城市
        for (String cityName : cityNameToIdMap.keySet()) {
            if (message.contains(cityName)) {
                cityIds.add(cityNameToIdMap.get(cityName));
                addressDetail.append(cityName);
            }
        }

        // 查询地区
        for (String districtName : districtNameToIdMap.keySet()) {
            if (message.contains(districtName)) {
                districtIds.add(districtNameToIdMap.get(districtName));
                addressDetail.append(districtName);
            }
        }

        result.setProvinceIds(provinceIds.isEmpty() ? null : provinceIds);
        result.setCityIds(cityIds.isEmpty() ? null : cityIds);
        result.setDistrictIds(districtIds.isEmpty() ? null : districtIds);
        result.setAddressDetail(addressDetail.length() > 0 ? addressDetail.toString() : null);
    }

    /**
     * 提取房间属性关键词
     * 例: "两室一厅" -> 提取 "两室"、"一厅" 对应的属性值ID
     */
    private void extractAttrKeywords(String message, KeywordExtractionResult result) {
        List<Long> attrValueIds = new ArrayList<>();

        for (String attrName : attrValueNameToIdMap.keySet()) {
            if (message.contains(attrName)) {
                attrValueIds.add(attrValueNameToIdMap.get(attrName));
            }
        }

        result.setAttrValueIds(attrValueIds.isEmpty() ? null : attrValueIds);
    }

    /**
     * 提取房间配套关键词 (type=2)
     * 例: "有空调、洗衣机" -> 提取对应的配套ID
     */
    private void extractFacilityKeywords(String message, KeywordExtractionResult result) {
        List<Long> facilityIds = new ArrayList<>();

        for (String facilityName : facilityNameToIdMap.keySet()) {
            if (message.contains(facilityName)) {
                facilityIds.add(facilityNameToIdMap.get(facilityName));
            }
        }

        result.setFacilityIds(facilityIds.isEmpty() ? null : facilityIds);
    }

    /**
     * 提取房间标签关键词 (type=2)
     * 例: "朝南、独卫" -> 提取对应的标签ID
     */
    private void extractLabelKeywords(String message, KeywordExtractionResult result) {
        List<Long> labelIds = new ArrayList<>();

        for (String labelName : labelNameToIdMap.keySet()) {
            if (message.contains(labelName)) {
                labelIds.add(labelNameToIdMap.get(labelName));
            }
        }

        result.setLabelIds(labelIds.isEmpty() ? null : labelIds);
    }

    /**
     * 提取价格范围
     * 支持多种表达方式:
     * - "2000-3000元/月"
     * - "租金大概2000"
     * - "预算3000以内"
     * - "最高5000"
     */
    private KeywordExtractionResult.PriceRange extractPriceRange(String message) {
        KeywordExtractionResult.PriceRange priceRange = new KeywordExtractionResult.PriceRange();
        priceRange.setMin(0);
        priceRange.setMax(Integer.MAX_VALUE);

        Matcher matcher = PRICE_PATTERN.matcher(message);
        if (matcher.find()) {
            try {
                if (matcher.group(1) != null && matcher.group(2) != null) {
                    int min = Integer.parseInt(matcher.group(1));
                    int max = Integer.parseInt(matcher.group(2));
                    priceRange.setMin(Math.min(min, max));
                    priceRange.setMax(Math.max(min, max));
                } else if (matcher.group(3) != null) {
                    priceRange.setMax(Integer.parseInt(matcher.group(3)));
                } else if (matcher.group(4) != null) {
                    priceRange.setMax(Integer.parseInt(matcher.group(4)));
                } else if (matcher.group(5) != null) {
                    priceRange.setMax(Integer.parseInt(matcher.group(5)));
                } else if (matcher.group(6) != null) {
                    priceRange.setMax(Integer.parseInt(matcher.group(6)));
                } else if (matcher.group(7) != null) {
                    priceRange.setMax(Integer.parseInt(matcher.group(7)));
                } else if (matcher.group(8) != null) {
                    priceRange.setMin(Integer.parseInt(matcher.group(8)));
                }
            } catch (Exception e) {
                log.warn("价格解析失败: {}", e.getMessage());
            }
        }

        return priceRange;
    }

    /**
     * 提取发布者类型
     * 例: "房东发布的房子" -> LANDLORD
     *     "官方房源" -> OFFICIAL
     */
    private String extractPublisherType(String message) {
        if (message.contains("房东")) {
            return "LANDLORD";
        }
        if (message.contains("官方")) {
            return "OFFICIAL";
        }
        return null;
    }

    /**
     * 提取入住时间
     */
    private LocalDate extractMoveInDate(String message) {
        LocalDate moveInDate = null;

        Matcher matcher = DATE_PATTERN.matcher(message);
        if (matcher.find()) {
            try {
                if (matcher.group(1) != null) {
                    int year = Integer.parseInt(matcher.group(1));
                    int month = Integer.parseInt(matcher.group(2));
                    int day = Integer.parseInt(matcher.group(3));
                    moveInDate = LocalDate.of(year, month, day);
                } else if (matcher.group(4) != null) {
                    int month = Integer.parseInt(matcher.group(4));
                    int day = Integer.parseInt(matcher.group(5));
                    moveInDate = LocalDate.of(LocalDate.now().getYear(), month, day);
                } else if (matcher.group(0).contains("今天")) {
                    moveInDate = LocalDate.now();
                } else if (matcher.group(0).contains("明天")) {
                    moveInDate = LocalDate.now().plusDays(1);
                } else if (matcher.group(0).contains("后天")) {
                    moveInDate = LocalDate.now().plusDays(2);
                } else if (matcher.group(0).contains("下周")) {
                    moveInDate = LocalDate.now().plusWeeks(1);
                } else if (matcher.group(0).contains("下月")) {
                    moveInDate = LocalDate.now().plusMonths(1);
                }
            } catch (Exception e) {
                log.warn("日期解析失败: {}", e.getMessage());
            }
        }

        return moveInDate;
    }

    /**
     * 提取租期
     */
    private Integer extractLeaseDuration(String message) {
        Integer leaseDuration = null;

        Matcher matcher = LEASE_DURATION_PATTERN.matcher(message);
        if (matcher.find()) {
            String matched = matcher.group(0);
            if (matched.contains("半年")) {
                leaseDuration = 6;
            } else if (matched.contains("一年")) {
                leaseDuration = 12;
            } else if (matched.contains("两年")) {
                leaseDuration = 24;
            } else if (matched.contains("三年")) {
                leaseDuration = 36;
            } else if (matched.contains("长期")) {
                leaseDuration = 12;
            } else if (matched.contains("短期")) {
                leaseDuration = 3;
            } else {
                Pattern numberPattern = Pattern.compile("(\\d+)");
                Matcher numberMatcher = numberPattern.matcher(matched);
                if (numberMatcher.find()) {
                    leaseDuration = Integer.parseInt(numberMatcher.group(1));
                }
            }
        }

        return leaseDuration;
    }

    /**
     * 计算提取的置信度
     * 置信度越高,说明提取的关键词越完整
     */
    private Double calculateConfidence(KeywordExtractionResult result) {
        double confidence = 0.0;

        // 有地区信息：+0.25
        if (result.getDistrictIds() != null && !result.getDistrictIds().isEmpty()) {
            confidence += 0.25;
        }

        // 有房间属性信息：+0.25
        if (result.getAttrValueIds() != null && !result.getAttrValueIds().isEmpty()) {
            confidence += 0.25;
        }

        // 有价格信息：+0.2
        if (result.getPriceRange() != null && result.getPriceRange().getMax() < Integer.MAX_VALUE) {
            confidence += 0.2;
        }

        // 有房间配套信息：+0.1
        if (result.getFacilityIds() != null && !result.getFacilityIds().isEmpty()) {
            confidence += 0.1;
        }

        // 有房间标签信息：+0.1
        if (result.getLabelIds() != null && !result.getLabelIds().isEmpty()) {
            confidence += 0.1;
        }

        // 有发布者类型：+0.05
        if (result.getPublisherType() != null) {
            confidence += 0.05;
        }

        return Math.min(confidence, 1.0);
    }

    @Override
    public boolean validateKeywords(KeywordExtractionResult result) {
        if (result == null) {
            return false;
        }

        // 至少需要一个有效的搜索条件
        boolean hasDistrict = result.getDistrictIds() != null && !result.getDistrictIds().isEmpty();
        boolean hasAttr = result.getAttrValueIds() != null && !result.getAttrValueIds().isEmpty();
        boolean hasPrice = result.getPriceRange() != null && result.getPriceRange().getMax() < Integer.MAX_VALUE;
        boolean hasFacility = result.getFacilityIds() != null && !result.getFacilityIds().isEmpty();

        return hasDistrict || hasAttr || hasPrice || hasFacility;
    }
}
