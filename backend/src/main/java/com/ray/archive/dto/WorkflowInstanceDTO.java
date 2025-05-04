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
import java.util.List;
import java.util.Map;

/**
 * 工作流实例数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "工作流实例信息")
public class WorkflowInstanceDTO {

    @ApiModelProperty(value = "实例ID", example = "1")
    private Long id;

    @ApiModelProperty(value = "工作流信息")
    private WorkflowDTO workflow;

    @ApiModelProperty(value = "业务键", example = "BORROW-2023-001")
    private String businessKey;

    @ApiModelProperty(value = "流程实例ID", example = "100001")
    private String processInstanceId;

    @ApiModelProperty(value = "流程类型", example = "BORROW_APPROVAL")
    private String processType;

    @ApiModelProperty(value = "发起人", example = "zhangsan")
    private String initiator;

    @ApiModelProperty(value = "状态", example = "RUNNING", notes = "RUNNING=运行中, COMPLETED=已完成, CANCELED=已取消, TERMINATED=已终止")
    private String status;

    @ApiModelProperty(value = "结果", example = "approved", notes = "approved=通过, rejected=拒绝")
    private String result;

    @ApiModelProperty(value = "业务数据JSON", example = "{\"archiveId\":123,\"borrowReason\":\"工作需要\",\"expectedReturnDate\":\"2023-05-20\"}")
    private String businessData;

    @ApiModelProperty(value = "流程变量JSON", example = "{\"approver\":\"lisi\",\"deptManager\":\"wangwu\"}")
    private String variables;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "开始时间", example = "2023-04-15 09:30:00")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "结束时间", example = "2023-04-16 14:20:00")
    private LocalDateTime endTime;

    @ApiModelProperty(value = "耗时(毫秒)", example = "86400000")
    private Long duration;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间", example = "2023-04-16 14:20:00")
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "当前任务", example = "部门经理审批")
    private String currentTask;

    @ApiModelProperty(value = "当前处理人", example = "wangwu")
    private String currentAssignee;

    @ApiModelProperty(value = "流程备注", example = "紧急审批事项")
    private String comment;

    @ApiModelProperty(value = "优先级", example = "1", notes = "0=低, 1=中, 2=高, 3=紧急")
    private Integer priority;

    @ApiModelProperty(value = "任务列表")
    private List<WorkflowTaskDTO> tasks;
} 