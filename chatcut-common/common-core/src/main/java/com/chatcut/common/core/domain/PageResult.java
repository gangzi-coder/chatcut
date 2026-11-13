package com.chatcut.common.core.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/** 分页结果，与 MyBatis-Plus 的 IPage 解耦，避免 service 层暴露 ORM 类型。 */
@Getter
@AllArgsConstructor
@Schema(description = "分页结果")
public class PageResult<T> {

    @Schema(description = "当前页数据列表")
    private final List<T> list;

    @Schema(description = "总条数", example = "42")
    private final long total;

    @Schema(description = "当前页码，从 1 开始", example = "1")
    private final long pageNo;

    @Schema(description = "每页条数（上限 200）", example = "10")
    private final long pageSize;

    public static <T> PageResult<T> of(List<T> list, long total, long pageNo, long pageSize) {
        return new PageResult<>(list, total, pageNo, pageSize);
    }

    public static <T> PageResult<T> empty(long pageNo, long pageSize) {
        return new PageResult<>(List.of(), 0L, pageNo, pageSize);
    }
}
