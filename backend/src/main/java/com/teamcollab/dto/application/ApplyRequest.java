package com.teamcollab.dto.application;

import com.teamcollab.common.valid.AddGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 入队申请请求DTO
 * <p>
 * 用于用户向目标团队提交入队申请，可选附加入队留言。
 * </p>
 *
 * @author teamcollab
 */
@Data
@Schema(description = "入队申请请求")
public class ApplyRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 申请留言，最长500字
     */
    @Size(max = 500, message = "申请留言不能超过500个字符", groups = AddGroup.class)
    @Schema(description = "申请留言", example = "我有5年Java开发经验，精通Spring Boot，希望加入贵团队共事")
    private String message;
}
