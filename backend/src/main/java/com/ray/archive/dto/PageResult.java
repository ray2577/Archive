package com.ray.archive.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 分页结果数据传输对象
 * @param <T> 列表项数据类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "分页数据结果")
public class PageResult<T> {

    @ApiModelProperty(value = "当前页数据列表")
    private List<T> records;

    @ApiModelProperty(value = "总记录数", example = "100")
    private long total;

    @ApiModelProperty(value = "总页数", example = "10")
    private int pages;

    @ApiModelProperty(value = "当前页码", example = "1")
    private int current;

    @ApiModelProperty(value = "每页大小", example = "10")
    private int size;

    @ApiModelProperty(value = "是否有下一页", example = "true")
    private boolean hasNext;

    @ApiModelProperty(value = "是否有上一页", example = "false")
    private boolean hasPrevious;

    /**
     * 通用构造函数
     */
    public PageResult(List<T> records, long total, int pages, int current) {
        this.records = records;
        this.total = total;
        this.pages = pages;
        this.current = current;
        this.size = current > 0 && pages > 0 ? (int) Math.ceil((double) total / pages) : 0;
        this.hasNext = current < pages;
        this.hasPrevious = current > 1;
    }

    /**
     * 从Spring Data Page创建PageResult对象
     */
    public static <T> PageResult<T> from(org.springframework.data.domain.Page<T> page) {
        return PageResult.<T>builder()
                .records(page.getContent())
                .total(page.getTotalElements())
                .pages(page.getTotalPages())
                .current(page.getNumber() + 1) // Convert from 0-based to 1-based for client
                .size(page.getSize())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    /**
     * 创建空的分页结果
     */
    public static <T> PageResult<T> empty(int pageSize) {
        return PageResult.<T>builder()
                .records(List.of())
                .total(0)
                .pages(0)
                .current(1)
                .size(pageSize)
                .hasNext(false)
                .hasPrevious(false)
                .build();
    }

}