package com.teamcollab.dto.user;

import com.teamcollab.common.valid.UpdateGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 更新用户资料请求DTO
 * <p>
 * 用于更新当前用户的头像和个人简介信息。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Schema(description = "更新用户资料请求")
public class UpdateProfileRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 头像URL
     */
    @Schema(description = "头像URL", example = "https://cdn.example.com/avatars/new.png")
    private String avatar;

    /**
     * 个人简介，最长500字
     */
    @Size(max = 500, message = "个人简介不能超过500个字符", groups = UpdateGroup.class)
    @Schema(description = "个人简介，最长500字", example = "一名热爱技术的全栈工程师，专注于Java和Vue开发")
    private String bio;
}
