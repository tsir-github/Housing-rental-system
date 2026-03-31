package com.atguigu.lease.web.app.controller.application;

import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.web.app.service.DirectMoveInApplicationService;
import com.atguigu.lease.web.app.vo.application.DirectMoveInApplicationDetailVO;
import com.atguigu.lease.web.app.vo.application.DirectMoveInApplicationSubmitVO;
import com.atguigu.lease.web.app.vo.room.RoomAvailabilityVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 租客直接入住申请控制器
 * 职责：租客提交和查看直接入住申请
 * 用户：租客（user_info表，user_type=租客）
 */
@Tag(name = "租客直接入住申请")
@RestController
@RequestMapping("/app/direct-move-in")
public class DirectMoveInApplicationController {

    @Autowired
    private DirectMoveInApplicationService directMoveInApplicationService;

    @Operation(summary = "租客提交直接入住申请")
    @PostMapping("/apply")
    public Result<Long> submitApplication(@RequestBody DirectMoveInApplicationSubmitVO submitVO) {
        // 设置当前登录用户ID
        submitVO.setUserId(LoginUserHolder.getLoginUser().getUserId());
        Long applicationId = directMoveInApplicationService.submitApplication(submitVO);
        return Result.ok(applicationId);
    }

    @Operation(summary = "租客查看自己的申请列表")
    @GetMapping("/my-applications")
    public Result<List<DirectMoveInApplicationDetailVO>> getMyApplications(@RequestParam Long userId) {
        List<DirectMoveInApplicationDetailVO> applications = 
            directMoveInApplicationService.getApplicationsByUserId(userId);
        return Result.ok(applications);
    }

    @Operation(summary = "租客查看申请详情")
    @GetMapping("/detail/{applicationId}")
    public Result<DirectMoveInApplicationDetailVO> getApplicationDetail(@PathVariable Long applicationId) {
        DirectMoveInApplicationDetailVO detail = 
            directMoveInApplicationService.getApplicationDetail(applicationId);
        return Result.ok(detail);
    }

    @Operation(summary = "检查房源可用性详情（含友好提示）")
    @GetMapping("/check-room/{roomId}")
    public Result<RoomAvailabilityVo> checkRoomAvailability(@PathVariable Long roomId) {
        RoomAvailabilityVo availability = directMoveInApplicationService.checkRoomAvailability(roomId);
        return Result.ok(availability);
    }

    @Operation(summary = "租客取消直接入住申请")
    @PostMapping("/cancel/{applicationId}")
    public Result<Void> cancelApplication(@PathVariable Long applicationId) {
        try {
            // 获取当前登录用户ID
            Long userId = LoginUserHolder.getLoginUser().getUserId();
            directMoveInApplicationService.cancelApplication(applicationId, userId);
            return Result.ok();
        } catch (RuntimeException e) {
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            return Result.fail("取消申请失败，请稍后重试");
        }
    }
}