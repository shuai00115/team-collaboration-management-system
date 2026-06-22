package com.teamcollab.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teamcollab.common.response.PageResult;
import com.teamcollab.common.response.Result;
import com.teamcollab.dto.skill.SkillQueryRequest;
import com.teamcollab.dto.skill.SkillResponse;
import com.teamcollab.service.SkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "技能管理", description = "技能标签查询接口")
@Slf4j
@RestController
@RequestMapping("/api/v1/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    @Operation(summary = "获取技能列表")
    @GetMapping
    public Result<PageResult<SkillResponse>> getSkillList(@Valid SkillQueryRequest query) {
        Page<SkillResponse> page = skillService.getSkillList(
                query.getPageNum(), query.getPageSize(), query.getCategory(), query.getKeyword());
        return Result.success(PageResult.of(page, page.getRecords()));
    }

    @Operation(summary = "获取技能分类")
    @GetMapping("/categories")
    public Result<List<String>> getCategories() {
        return Result.success(skillService.getCategories());
    }
}
