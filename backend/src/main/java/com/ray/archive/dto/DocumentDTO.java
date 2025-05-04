package com.ray.archive.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 文档数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "文档信息")
public class DocumentDTO {

    @ApiModelProperty(value = "文档ID", example = "1")
    private Long id;

    @NotBlank(message = "文档编号不能为空")
    @Size(max = 50, message = "文档编号长度不能超过50个字符")
    @ApiModelProperty(value = "文档编号", required = true, example = "DOC20230001")
    private String code;

    @NotBlank(message = "文档名称不能为空")
    @Size(max = 200, message = "文档名称长度不能超过200个字符")
    @ApiModelProperty(value = "文档名称", required = true, example = "技术方案设计文档")
    private String name;

    @ApiModelProperty(value = "文档类型", example = "2", notes = "1=文本文档，2=PDF文档，3=Excel表格，4=Word文档")
    private String type;

    @ApiModelProperty(value = "文档分类", example = "技术文档")
    private String category;

    @Size(max = 2000, message = "文档描述长度不能超过2000个字符")
    @ApiModelProperty(value = "文档描述", example = "包含系统架构设计和技术选型的详细文档")
    private String description;

    @ApiModelProperty(value = "关键词列表", example = "[\"架构设计\", \"技术选型\", \"系统方案\"]")
    private List<String> keywords;

    @ApiModelProperty(value = "文件URL", example = "files/4a5b8c2d-1234-5678-90ab-cdef01234567.pdf")
    private String fileUrl;

    @ApiModelProperty(value = "文件类型", example = "pdf")
    private String fileType;

    @ApiModelProperty(value = "文件大小(字节)", example = "1024000")
    private Long fileSize;

    @ApiModelProperty(value = "下载次数", example = "5")
    private Integer downloadCount;

    @ApiModelProperty(value = "状态", example = "ACTIVE", notes = "ACTIVE=活跃, ARCHIVED=归档, SHARED=已分享, DRAFT=草稿, DELETED=已删除")
    private String status;

    @ApiModelProperty(value = "是否已分享", example = "false")
    private Boolean shared;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "分享过期时间", example = "2023-06-30 23:59:59")
    private LocalDateTime shareExpireTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间", example = "2023-01-15 10:30:00")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间", example = "2023-04-20 14:15:00")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "创建人", example = "zhangsan")
    private String creator;

    @ApiModelProperty(value = "更新人", example = "lisi")
    private String updater;
} 