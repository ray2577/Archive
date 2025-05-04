package com.ray.archive.controller;

import com.ray.archive.common.ApiResponse;
import com.ray.archive.dto.PageResult;
import com.ray.archive.dto.WorkflowDTO;
import com.ray.archive.dto.WorkflowInstanceDTO;
import com.ray.archive.dto.WorkflowTaskDTO;
import com.ray.archive.service.WorkflowService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 工作流管理控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/workflows")
@Api(tags = "工作流管理", description = "提供工作流的定义、部署、执行和任务处理功能")
public class WorkflowController {

    private final WorkflowService workflowService;
    
    /* ==================== 流程定义相关 ==================== */
    
    @PostMapping
    @ApiOperation("创建工作流定义")
    public ApiResponse<WorkflowDTO> createWorkflow(@Valid @RequestBody WorkflowDTO workflowDTO) {
        return ApiResponse.success(workflowService.createWorkflow(workflowDTO));
    }
    
    @PutMapping("/{id}")
    @ApiOperation("更新工作流定义")
    public ApiResponse<WorkflowDTO> updateWorkflow(
            @PathVariable Long id,
            @Valid @RequestBody WorkflowDTO workflowDTO) {
        return ApiResponse.success(workflowService.updateWorkflow(id, workflowDTO));
    }
    
    @GetMapping("/{id}")
    @ApiOperation("获取工作流定义详情")
    public ApiResponse<WorkflowDTO> getWorkflow(@PathVariable Long id) {
        return ApiResponse.success(workflowService.getWorkflowById(id));
    }
    
    @GetMapping
    @ApiOperation("分页获取工作流定义列表")
    public ApiResponse<PageResult<WorkflowDTO>> getWorkflows(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "createTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        return ApiResponse.success(workflowService.getWorkflows(
                page, pageSize, name, category, status, sortBy, sortDirection));
    }
    
    @DeleteMapping("/{id}")
    @ApiOperation("删除工作流定义")
    public ApiResponse<Boolean> deleteWorkflow(@PathVariable Long id) {
        return ApiResponse.success(workflowService.deleteWorkflow(id));
    }
    
    @PostMapping("/{id}/deploy")
    @ApiOperation("发布工作流")
    public ApiResponse<WorkflowDTO> deployWorkflow(@PathVariable Long id) {
        return ApiResponse.success(workflowService.deployWorkflow(id));
    }
    
    /* ==================== 流程实例相关 ==================== */
    
    @PostMapping("/instances/start")
    @ApiOperation("启动工作流实例")
    public ApiResponse<WorkflowInstanceDTO> startWorkflow(
            @RequestParam Long workflowId,
            @RequestParam String businessKey,
            @RequestBody(required = false) Map<String, Object> variables) {
        if (variables == null) {
            variables = new HashMap<>();
        }
        return ApiResponse.success(workflowService.startWorkflow(workflowId, businessKey, variables));
    }
    
    @GetMapping("/instances/{instanceId}")
    @ApiOperation("获取工作流实例详情")
    public ApiResponse<WorkflowInstanceDTO> getWorkflowInstance(@PathVariable Long instanceId) {
        return ApiResponse.success(workflowService.getWorkflowInstance(instanceId));
    }
    
    @GetMapping("/instances")
    @ApiOperation("分页获取工作流实例列表")
    public ApiResponse<PageResult<WorkflowInstanceDTO>> getWorkflowInstances(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String processName,
            @RequestParam(required = false) String initiator,
            @RequestParam(required = false) String processType,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(workflowService.getWorkflowInstances(
                page, pageSize, processName, initiator, processType, status));
    }
    
    @GetMapping("/my-instances")
    @ApiOperation("获取我的工作流实例")
    public ApiResponse<PageResult<WorkflowInstanceDTO>> getMyWorkflowInstances(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam Long userId,
            @RequestParam(required = false) String status) {
        return ApiResponse.success(workflowService.getMyWorkflowInstances(page, pageSize, userId, status));
    }
    
    @GetMapping("/history")
    @ApiOperation("分页获取工作流历史记录")
    public ApiResponse<PageResult<WorkflowInstanceDTO>> getWorkflowHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String processName,
            @RequestParam(required = false) String initiator,
            @RequestParam(required = false) String processType,
            @RequestParam(required = false) String result) {
        return ApiResponse.success(workflowService.getWorkflowHistory(
                page, pageSize, processName, initiator, processType, result));
    }
    
    @GetMapping("/instances/{processInstanceId}/diagram")
    @ApiOperation("获取流程图")
    public ResponseEntity<Resource> getProcessDiagram(@PathVariable String processInstanceId) {
        Resource resource = workflowService.getProcessDiagram(processInstanceId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"process-diagram.png\"")
                .body(resource);
    }
    
    @PostMapping("/history/export")
    @ApiOperation("导出工作流历史")
    public ResponseEntity<Resource> exportWorkflowHistory(@RequestBody(required = false) Map<String, Object> filters) {
        if (filters == null) {
            filters = new HashMap<>();
        }
        Resource resource = workflowService.exportWorkflowHistory(filters);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"workflow-history.xlsx\"")
                .body(resource);
    }
    
    /* ==================== 任务相关 ==================== */
    
    @GetMapping("/tasks/todo")
    @ApiOperation("获取用户待办任务")
    public ApiResponse<PageResult<WorkflowTaskDTO>> getTodoTasks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam String assignee) {
        return ApiResponse.success(workflowService.getTodoTasks(page, pageSize, assignee));
    }
    
    @GetMapping("/tasks/done")
    @ApiOperation("获取用户已办任务")
    public ApiResponse<PageResult<WorkflowTaskDTO>> getDoneTasks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam String assignee) {
        return ApiResponse.success(workflowService.getDoneTasks(page, pageSize, assignee));
    }
    
    @PostMapping("/tasks/{taskId}/complete")
    @ApiOperation("完成任务")
    public ApiResponse<WorkflowTaskDTO> completeTask(
            @PathVariable String taskId,
            @RequestParam String outcome,
            @RequestParam(required = false) String comment,
            @RequestBody(required = false) Map<String, Object> formData) {
        if (formData == null) {
            formData = new HashMap<>();
        }
        return ApiResponse.success(workflowService.completeTask(taskId, outcome, formData, comment));
    }
    
    @PostMapping("/tasks/{taskId}/reject")
    @ApiOperation("拒绝任务")
    public ApiResponse<WorkflowTaskDTO> rejectTask(
            @PathVariable String taskId,
            @RequestParam String reason) {
        return ApiResponse.success(workflowService.rejectTask(taskId, reason));
    }
    
    @PostMapping("/tasks/{taskId}/transfer")
    @ApiOperation("转交任务")
    public ApiResponse<WorkflowTaskDTO> transferTask(
            @PathVariable String taskId,
            @RequestParam String assignee,
            @RequestParam String reason) {
        return ApiResponse.success(workflowService.transferTask(taskId, assignee, reason));
    }
    
    @PostMapping("/tasks/{taskId}/delegate")
    @ApiOperation("委派任务")
    public ApiResponse<WorkflowTaskDTO> delegateTask(
            @PathVariable String taskId,
            @RequestParam String assignee,
            @RequestParam String reason) {
        return ApiResponse.success(workflowService.delegateTask(taskId, assignee, reason));
    }
} 