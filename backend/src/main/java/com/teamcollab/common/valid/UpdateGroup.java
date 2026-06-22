package com.teamcollab.common.valid;

/**
 * 更新操作的校验分组接口
 * <p>
 * 用于 {@code @Validated} 注解的分组校验，标记属于"更新"场景的校验规则。
 * 在更新操作中，通常需要校验 ID 不能为空等场景。
 * </p>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 *   // DTO 中定义分组
 *   @NotNull(message = "项目ID不能为空", groups = {UpdateGroup.class})
 *   private Long id;
 *
 *   @NotBlank(message = "项目名称不能为空", groups = {AddGroup.class, UpdateGroup.class})
 *   private String name;
 *
 *   // Controller 中指定分组
 *   @PutMapping
 *   public Result<Void> update(@Validated(UpdateGroup.class) @RequestBody ProjectDTO dto) { }
 * }</pre>
 *
 * @author TeamCollab
 * @see AddGroup
 */
public interface UpdateGroup {
    // 标记接口，无需定义方法
}
