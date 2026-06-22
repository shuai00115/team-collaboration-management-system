package com.teamcollab.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.teamcollab.entity.StageTemplate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 阶段模板 Mapper 接口
 * <p>
 * 提供项目阶段模板相关的数据库操作，包括获取所有模板及其阶段数量统计等功能。
 * 阶段模板用于快速创建项目预设阶段结构。
 * </p>
 *
 * @author teamcollab
 * @since 1.0.0
 */
@Mapper
public interface StageTemplateMapper extends BaseMapper<StageTemplate> {

    /**
     * 查询所有阶段模板（含阶段数量统计）
     *
     * @return 模板列表，每项含阶段数量 stage_count
     */
    List<Map<String, Object>> selectAllWithCount();
}
