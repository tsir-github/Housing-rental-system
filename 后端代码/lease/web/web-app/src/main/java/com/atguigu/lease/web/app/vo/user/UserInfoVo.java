package com.atguigu.lease.web.app.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "用户基本信息")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoVo {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "用户头像")
    private String avatarUrl;

    @Schema(description = "用户类型：1-租客，2-房东")
    private Integer userType;

    @Schema(description = "用户状态：1-正常，2-暂停，3-封禁")
    private Integer status;

    @Schema(description = "是否为房东")
    private Boolean isLandlord;

    @Schema(description = "房东权限列表（仅房东用户有）")
    private List<String> permissions;

    // 保持向后兼容的构造函数
    public UserInfoVo(String nickname, String avatarUrl) {
        this.nickname = nickname;
        this.avatarUrl = avatarUrl;
    }
}