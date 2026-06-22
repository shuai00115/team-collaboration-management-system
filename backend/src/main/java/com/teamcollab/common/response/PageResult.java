package com.teamcollab.common.response;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 分页查询统一响应封装类
 * <p>
 * 用于封装分页查询的返回结果，包含分页信息和当前页的数据记录列表。
 * 通常与 MyBatis-Plus 的 {@link IPage} 配合使用，通过 {@link #of(IPage, List)} 方法快速构建。
 * </p>
 *
 * <p><b>响应格式示例：</b></p>
 * <pre>{@code
 *   {
 *     "pageNum": 1,
 *     "pageSize": 10,
 *     "total": 100,
 *     "totalPages": 10,
 *     "records": [ ... ]
 *   }
 * }</pre>
 *
 * @param <T> 分页记录的数据类型
 * @author TeamCollab
 * @see IPage
 */
@Data
@Schema(description = "分页查询结果")
public class PageResult<T> {

    /** 当前页码，从 1 开始 */
    @Schema(description = "当前页码", example = "1")
    private int pageNum;

    /** 每页显示的记录数 */
    @Schema(description = "每页记录数", example = "10")
    private int pageSize;

    /** 符合条件的总记录数 */
    @Schema(description = "总记录数", example = "100")
    private long total;

    /** 总页数，根据 total 和 pageSize 计算得出 */
    @Schema(description = "总页数", example = "10")
    private int totalPages;

    /** 当前页的数据记录列表 */
    @Schema(description = "当前页数据记录")
    private List<T> records;

    /**
     * 无参构造函数
     */
    public PageResult() {
    }

    /**
     * 全参构造函数
     *
     * @param pageNum    当前页码
     * @param pageSize   每页记录数
     * @param total      总记录数
     * @param totalPages 总页数
     * @param records    当前页的数据记录
     */
    public PageResult(int pageNum, int pageSize, long total, int totalPages, List<T> records) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.totalPages = totalPages;
        this.records = records;
    }

    /**
     * 通过 MyBatis-Plus 的分页对象和转换后的记录列表构建分页结果
     * <p>
     * 典型用法：先通过 MyBatis-Plus 执行分页查询获得 {@link IPage} 对象，
     * 再将其中实体记录转换为 DTO 列表后调用此方法。
     * </p>
     *
     * <p>使用示例：</p>
     * <pre>{@code
     *   IPage<User> page = userService.page(new Page<>(pageNum, pageSize), wrapper);
     *   List<UserVO> voList = page.getRecords().stream()
     *           .map(UserVO::fromEntity)
     *           .collect(Collectors.toList());
     *   return PageResult.of(page, voList);
     * }</pre>
     *
     * @param page    MyBatis-Plus 的分页对象，提供分页基本参数
     * @param records 转换后的数据记录列表（通常为 DTO/VO 列表）
     * @param <T>     记录的数据类型
     * @return 封装好的分页结果
     */
    public static <T> PageResult<T> of(IPage<?> page, List<T> records) {
        PageResult<T> result = new PageResult<>();
        result.setPageNum((int) page.getCurrent());
        result.setPageSize((int) page.getSize());
        result.setTotal(page.getTotal());
        result.setTotalPages((int) page.getPages());
        result.setRecords(records);
        return result;
    }
}
