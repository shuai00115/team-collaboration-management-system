package com.teamcollab.common.valid;

/**
 * 添加操作的校验分组接口
 * <p>
 * 用于 {@code @Validated} 注解的分组校验，标记属于"新增"场景的校验规则。
 * 在新增操作中，通常不需要校验 ID 是否为空等场景。
 * </p>
 *
 * <p>使用示例：</p>
 * <pre>{@code
 *   // DTO 中定义分组
 *   @NotBlank(message = "项目名称不能为空", groups = {AddGroup.class, UpdateGroup.class})
 *   private String name;
 *
 *   @NotNull(message = "团队ID不能为空", groups = {AddGroup.class})
 *   private Long teamId;
 *
 *   // Controller 中指定分组
 *   @PostMapping
 *   public Result<Void> create(@Validated(AddGroup.class) @RequestBody ProjectDTO dto) { }
 * }</pre>
 *
 * @author TeamCollab
 * @see UpdateGroup
 */
public interface AddGroup {
    // 标记接口，无需定义方法
}
