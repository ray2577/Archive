package com.ray.archive.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 档案模板数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "档案模板信息")
public class ArchiveTemplateDTO {

    @ApiModelProperty(value = "模板ID", example = "1")
    private Long id;

    @NotBlank(message = "模板名称不能为空")
    @Size(max = 100, message = "模板名称长度不能超过100个字符")
    @ApiModelProperty(value = "模板名称", required = true, example = "合同档案模板")
    private String name;

    @NotBlank(message = "模板类型不能为空")
    @ApiModelProperty(value = "模板类型", required = true, example = "WORD", notes = "WORD, PDF, HTML, EXCEL")
    private String type;

    @Size(max = 2000, message = "模板描述长度不能超过2000个字符")
    @ApiModelProperty(value = "模板描述", example = "用于生成标准合同档案文件的Word模板")
    private String description;

    @NotBlank(message = "适用档案类别不能为空")
    @ApiModelProperty(value = "适用档案类别", required = true, example = "合同档案")
    private String category;

    @ApiModelProperty(value = "模板文件URL", example = "templates/contracts/template-v1.docx")
    private String fileUrl;

    @ApiModelProperty(value = "模板文件存储路径", example = "/data/templates/contracts/template-v1.docx")
    private String filePath;

    @ApiModelProperty(value = "缩略图URL", example = "templates/contracts/template-v1-thumb.png")
    private String thumbnail;

    @ApiModelProperty(value = "模板内容", example = "<html><body><h1>{{title}}</h1><p>{{content}}</p></body></html>")
    private String content;

    @ApiModelProperty(value = "模板字段定义", example = "[{\"name\":\"title\",\"type\":\"string\",\"label\":\"标题\"},{\"name\":\"content\",\"type\":\"text\",\"label\":\"内容\"}]")
    private String schema;

    @ApiModelProperty(value = "是否启用", example = "true")
    private Boolean isActive;

    @ApiModelProperty(value = "版本号", example = "1")
    private Integer version;

    @ApiModelProperty(value = "是否最新版本", example = "true")
    private Boolean isLatestVersion;

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
} 