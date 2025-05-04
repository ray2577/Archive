package com.ray.archive.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 工作流任务数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "工作流任务信息")
public class WorkflowTaskDTO {

    @ApiModelProperty(value = "任务ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "流程实例ID", example = "123")
    private Long workflowInstanceId;

    @ApiModelProperty(value = "流程实例业务键", example = "BORROW-2023-001")
    private String businessKey;

    @ApiModelProperty(value = "流程名称", example = "档案借阅审批")
    private String processName;

    @ApiModelProperty(value = "引擎任务ID", example = "2001")
    private String taskId;

    @ApiModelProperty(value = "任务名称", example = "部门经理审批")
    private String taskName;

    @ApiModelProperty(value = "任务定义Key", example = "deptManagerApproval")
    private String taskDefinitionKey;

    @ApiModelProperty(value = "处理人", example = "wangwu")
    private String assignee;

    @ApiModelProperty(value = "任务所有者", example = "zhangsan")
    private String owner;

    @ApiModelProperty(value = "状态", example = "PENDING", notes = "PENDING=待处理, COMPLETED=已完成, CANCELED=已取消")
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间", example = "2023-04-15 10:30:00")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "完成时间", example = "2023-04-16 14:20:00")
    private LocalDateTime completeTime;

    @ApiModelProperty(value = "处理时长(毫秒)", example = "86400000")
    private Long duration;

    @ApiModelProperty(value = "处理意见", example = "同意该申请")
    private String comment;

    @ApiModelProperty(value = "表单数据JSON", example = "{\"approveResult\":\"同意\",\"comments\":\"符合规定\"}")
    private String formData;

    @ApiModelProperty(value = "处理结果", example = "approve", notes = "approve=同意, reject=拒绝, transfer=转交")
    private String outcome;

    @ApiModelProperty(value = "委派给谁", example = "lisi")
    private String delegatedTo;

    @ApiModelProperty(value = "转交给谁", example = "zhaoliu")
    private String transferredTo;

    @ApiModelProperty(value = "转交原因", example = "因本人休假，转交给替岗人员处理")
    private String transferReason;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间", example = "2023-04-16 14:20:00")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "优先级", example = "1", notes = "0=低, 1=中, 2=高, 3=紧急")
    private Integer priority;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "截止日期", example = "2023-04-18 23:59:59")
    private LocalDateTime dueDate;

    @ApiModelProperty(value = "是否逾期", example = "false")
    private Boolean isOverdue;
} 