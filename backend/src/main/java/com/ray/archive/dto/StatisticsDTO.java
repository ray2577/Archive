package com.ray.archive.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

/**
 * 档案统计数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "档案统计数据")
public class StatisticsDTO {

    @ApiModelProperty(value = "档案总数", example = "1250")
    private Long total;

    @ApiModelProperty(value = "可用档案数", example = "1100")
    private Long available;

    @ApiModelProperty(value = "借出档案数", example = "150")
    private Long borrowed;
    
    @ApiModelProperty(value = "处理中档案数", example = "30")
    private Long processing;
    
    @ApiModelProperty(value = "已归档档案数", example = "200")
    private Long archived;
    
    @ApiModelProperty(value = "草稿档案数", example = "50")
    private Long draft;
    
    @ApiModelProperty(value = "已分享档案数", example = "80")
    private Long shared;

    @ApiModelProperty(value = "本月新增档案数", example = "45")
    private Long newThisMonth;

    @ApiModelProperty(value = "按类型统计", notes = "键为档案类型，值为数量")
    private Map<String, Long> typeDistribution;
    
    @ApiModelProperty(value = "按分类统计", notes = "键为档案分类，值为数量")
    private Map<String, Long> categoryDistribution;

    @ApiModelProperty(value = "按状态统计", notes = "键为档案状态，值为数量")
    private Map<String, Long> statusDistribution;

    @ApiModelProperty(value = "按月统计", notes = "键为月份(yyyy-MM格式)，值为该月档案数量")
    private Map<String, Long> monthlyDistribution;

    @ApiModelProperty(value = "借阅次数", example = "250")
    private Long borrowCount;

    @ApiModelProperty(value = "借阅人次", example = "120")
    private Long borrowerCount;

    @ApiModelProperty(value = "高频借阅档案", notes = "键为档案ID，值为借阅次数")
    private Map<Long, Long> highFrequencyArchives;

    @ApiModelProperty(value = "平均借阅时长(天)", example = "7.5")
    private Double averageBorrowDuration;
    
    @ApiModelProperty(value = "总下载次数", example = "2500")
    private Long totalDownloads;
    
    @ApiModelProperty(value = "热门档案", notes = "键为档案ID，值为下载次数")
    private Map<Long, Integer> hotArchives;

    @ApiModelProperty(value = "最近档案", notes = "最近添加的档案列表")
    private List<ArchiveDTO> recentArchives;
    
    @ApiModelProperty(value = "最常借阅档案", notes = "借阅次数最多的档案列表")
    private List<ArchiveDTO> mostBorrowedArchives;
    
    @ApiModelProperty(value = "最后更新时间", example = "2023-05-15 14:30:00")
    private LocalDateTime lastUpdated;
} 