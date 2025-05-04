package com.ray.archive.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 文档统计数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "文档统计数据")
public class DocumentStatisticsDTO {

    @ApiModelProperty(value = "文档总数", example = "1250")
    private Long total;

    @ApiModelProperty(value = "活跃文档数", example = "1000")
    private Long active;

    @ApiModelProperty(value = "归档文档数", example = "200")
    private Long archived;

    @ApiModelProperty(value = "草稿文档数", example = "50")
    private Long draft;

    @ApiModelProperty(value = "已分享文档数", example = "80")
    private Long shared;

    @ApiModelProperty(value = "本月新增文档数", example = "45")
    private Long newThisMonth;

    @ApiModelProperty(value = "按类型统计", notes = "键为文档类型，值为数量")
    private Map<String, Long> typeDistribution;

    @ApiModelProperty(value = "按分类统计", notes = "键为文档分类，值为数量")
    private Map<String, Long> categoryDistribution;

    @ApiModelProperty(value = "按状态统计", notes = "键为文档状态，值为数量")
    private Map<String, Long> statusDistribution;

    @ApiModelProperty(value = "按月统计", notes = "键为月份(yyyy-MM格式)，值为该月文档数量")
    private Map<String, Long> monthlyDistribution;

    @ApiModelProperty(value = "总下载次数", example = "2500")
    private Long totalDownloads;

    @ApiModelProperty(value = "热门文档", notes = "键为文档ID，值为下载次数")
    private Map<Long, Integer> hotDocuments;
} 