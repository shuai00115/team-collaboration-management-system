package com.teamcollab.controller;

import com.teamcollab.common.response.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <p>
 * 健康检查控制器
 * </p>
 *
 * @author teamcollab
 * @since 2026-06-22
 */
@Tag(name = "健康检查", description = "服务健康状态检查")
@RestController
public class HealthController {

    @Operation(summary = "健康检查")
    @GetMapping("/api/v1/health")
    public Result<Map<String, Object>> health() {
        Map<String, Object> info = new LinkedHashMap<>();
        info.put("status", "UP");
        info.put("service", "团队协作管理系统");
        info.put("version", "1.0.0");
        info.put("timestamp", LocalDateTime.now().toString());
        return Result.success(info);
    }
}
