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
 * 工作流数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "工作流信息")
public class WorkflowDTO {

    @ApiModelProperty(value = "工作流ID", example = "1")
    private Long id;

    @NotBlank(message = "工作流编码不能为空")
    @Size(max = 50, message = "工作流编码长度不能超过50个字符")
    @ApiModelProperty(value = "工作流编码", required = true, example = "LEAVE_APPROVAL")
    private String code;

    @NotBlank(message = "工作流名称不能为空")
    @Size(max = 100, message = "工作流名称长度不能超过100个字符")
    @ApiModelProperty(value = "工作流名称", required = true, example = "请假审批流程")
    private String name;

    @NotBlank(message = "工作流分类不能为空")
    @ApiModelProperty(value = "工作流分类", required = true, example = "人事流程")
    private String category;

    @Size(max = 2000, message = "工作流描述长度不能超过2000个字符")
    @ApiModelProperty(value = "工作流描述", example = "员工请假审批流程，包含直属领导和部门经理两级审批")
    private String description;

    @ApiModelProperty(value = "状态", example = "ACTIVE", notes = "ACTIVE=活跃, INACTIVE=未启用, DEPRECATED=已弃用")
    private String status;

    @ApiModelProperty(value = "流程定义JSON", example = "{\"nodes\":[...],\"edges\":[...]}")
    private String definitionJson;

    @ApiModelProperty(value = "工作流引擎中的流程定义ID", example = "leave_approval:1:1005")
    private String processDefinitionId;

    @ApiModelProperty(value = "工作流引擎中的流程定义Key", example = "leave_approval")
    private String processDefinitionKey;

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

    private String processType;
    private String definition;

} 