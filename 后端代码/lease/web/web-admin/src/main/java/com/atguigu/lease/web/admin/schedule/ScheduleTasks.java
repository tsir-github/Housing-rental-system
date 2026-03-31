package com.atguigu.lease.web.admin.schedule;

import com.atguigu.lease.model.entity.LeaseAgreement;
import com.atguigu.lease.model.enums.LeaseStatus;
import com.atguigu.lease.web.admin.service.LeaseAgreementService;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ScheduleTasks {

    /*@Scheduled(cron = "* * * * * *")
    public void test(){
        System.out.println(new Date());
    }*/
    @Autowired
    private LeaseAgreementService service;

    //检查租约状态
    @Scheduled(cron = "0 0 0 * * *")
    public void checkLeaseStatus(){

        LambdaUpdateWrapper<LeaseAgreement> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.le(LeaseAgreement::getLeaseEndDate,new Date());// 到期日期<=今天
        updateWrapper.in(LeaseAgreement::getStatus,LeaseStatus.SIGNED,LeaseStatus.WITHDRAWING);// 状态为签约中/退租中
        updateWrapper.set(LeaseAgreement::getStatus, LeaseStatus.EXPIRED); // 设为过期
        service.update(updateWrapper);
    }
}
