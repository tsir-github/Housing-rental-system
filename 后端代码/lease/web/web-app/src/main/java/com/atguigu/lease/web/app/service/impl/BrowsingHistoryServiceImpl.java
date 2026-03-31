package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.model.entity.BrowsingHistory;
import com.atguigu.lease.web.app.mapper.BrowsingHistoryMapper;
import com.atguigu.lease.web.app.service.BrowsingHistoryService;
import com.atguigu.lease.web.app.vo.history.HistoryItemVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author liubo
 * @description 针对表【browsing_history(浏览历史)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
public class BrowsingHistoryServiceImpl extends ServiceImpl<BrowsingHistoryMapper, BrowsingHistory>
        implements BrowsingHistoryService {

    @Autowired
    private BrowsingHistoryMapper browsingHistoryMapper;
    @Override
    public IPage<HistoryItemVo> pageHistoryItemByUserId(Page<HistoryItemVo> page, Long userId) {
        return browsingHistoryMapper.pageHistoryItemByUserId(page, userId);
    }

    @Override//**触发保存浏览历史**保存浏览历史的动作应该在浏览房间详情时触发
    @Async//异步操作
    public void saveHistory(Long userId, Long id) {
        System.out.println("保存浏览历史-"+Thread.currentThread().getName());
        LambdaQueryWrapper<BrowsingHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BrowsingHistory::getUserId,userId);
        queryWrapper.eq(BrowsingHistory::getRoomId,id);
        BrowsingHistory browsingHistory = browsingHistoryMapper.selectOne(queryWrapper);

        if (browsingHistory!=null){
            browsingHistory.setBrowseTime(new Date());
            browsingHistoryMapper.updateById(browsingHistory);
        }else {
            BrowsingHistory history = new BrowsingHistory();
            history.setUserId(userId);
            history.setRoomId(id);
            history.setBrowseTime(new Date());
            browsingHistoryMapper.insert(history);
        }


    }
}