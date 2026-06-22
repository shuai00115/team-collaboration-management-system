package com.teamcollab.common.util;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.teamcollab.common.response.PageResult;

import java.util.List;

/**
 * 分页工具类
 * <p>
 * 提供 MyBatis-Plus 分页对象到统一分页响应格式的转换方法。
 * 将 {@link IPage} 中的分页参数和记录列表封装为 {@link PageResult} 对象。
 * </p>
 *
 * @author TeamCollab
 * @see PageResult
 */
public final class PageUtils {

    /**
     * 私有构造函数，防止外部实例化工具类
     */
    private PageUtils() {
        throw new UnsupportedOperationException("工具类不可实例化");
    }

    /**
     * 将 MyBatis-Plus 分页对象转换为统一的分页结果
     * <p>
     * 从 {@link IPage} 中提取分页参数（页码、每页大小、总记录数、总页数），
     * 并结合传入的已转换记录列表，构建 {@link PageResult} 对象。
     * </p>
     *
     * <p>使用示例：</p>
     * <pre>{@code
     *   IPage<User> page = userService.page(new Page<>(1, 10));
     *   List<UserVO> records = page.getRecords().stream()
     *           .map(UserVO::fromEntity)
     *           .collect(Collectors.toList());
     *   PageResult<UserVO> result = PageUtils.toPageResult(page, records);
     * }</pre>
     *
     * @param page    MyBatis-Plus 的分页对象
     * @param records 转换后的数据记录列表（通常为 DTO/VO）
     * @param <T>     记录的数据类型
     * @return 封装好的分页结果
     */
    public static <T> PageResult<T> toPageResult(IPage<?> page, List<T> records) {
        return PageResult.of(page, records);
    }
}
