package com.ray.archive.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 档案数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "档案信息")
public class ArchiveDTO {

    @ApiModelProperty(value = "档案ID", example = "1")
    private Long id;

    @NotBlank(message = "档案编号不能为空")
    @Size(max = 50, message = "档案编号长度不能超过50个字符")
    @ApiModelProperty(value = "档案编号", required = true, example = "ARC20230001")
    private String fileNumber;

    @NotBlank(message = "档案名称不能为空")
    @Size(max = 200, message = "档案名称长度不能超过200个字符")
    @ApiModelProperty(value = "档案名称", required = true, example = "财务报表2023")
    private String title;

    @ApiModelProperty(value = "档案类型", example = "FINANCIAL")
    private String type;

    @ApiModelProperty(value = "档案类别", example = "季度报表")
    private String category;

    @ApiModelProperty(value = "存放位置", example = "A-01-02")
    private String location;

    @ApiModelProperty(value = "负责人", example = "张三")
    private String responsible;

    @ApiModelProperty(value = "档案状态", example = "AVAILABLE", notes = "可选值: AVAILABLE(在库), BORROWED(借出), PROCESSING(处理中), ARCHIVED(已归档), SHARED(已分享), DRAFT(草稿), DELETED(已删除)")
    private String status;

    @Size(max = 2000, message = "档案描述长度不能超过2000个字符")
    @ApiModelProperty(value = "档案描述", example = "2023年第一季度财务报表，包含资产负债表和损益表")
    private String description;

    @Size(max = 2000, message = "备注长度不能超过2000个字符")
    @ApiModelProperty(value = "备注", example = "该报表已经过财务部门审核")
    private String remarks;

    @ApiModelProperty(value = "关键词列表", example = "[\"财务\", \"季度\", \"报表\"]")
    private List<String> keywords;

    @ApiModelProperty(value = "文件URL", example = "files/4a5b8c2d-1234-5678-90ab-cdef01234567.pdf")
    private String fileUrl;

    @ApiModelProperty(value = "文件路径", example = "/data/archives/financials/4a5b8c2d-1234-5678-90ab-cdef01234567.pdf")
    private String filePath;

    @ApiModelProperty(value = "文件类型", example = "pdf")
    private String fileType;

    @ApiModelProperty(value = "文件大小(字节)", example = "1024000")
    private Long fileSize;

    @ApiModelProperty(value = "下载次数", example = "5")
    private Integer downloadCount;

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

    @ApiModelProperty(value = "创建人", example = "admin")
    private String creator;

    @ApiModelProperty(value = "更新人", example = "admin")
    private String updater;

    @ApiModelProperty(value = "是否删除", example = "false")
    private Boolean deleted;

    // 档案日期相关
    private LocalDateTime archiveDate;  // 归档日期
    private LocalDateTime expirationDate;  // 过期日期
    private LocalDateTime lastDownloadTime;  // 最后下载时间
} 