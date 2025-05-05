package com.ray.archive.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 档案借阅记录数据传输对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "档案借阅记录")
public class BorrowRecordDTO {

    @ApiModelProperty(value = "借阅记录ID", example = "1")
    private Long id;

    @NotNull(message = "档案ID不能为空")
    @ApiModelProperty(value = "档案ID", required = true, example = "1")
    private Long archiveId;

    @ApiModelProperty(value = "档案名称", example = "财务报表2023")
    private String archiveTitle;

    @ApiModelProperty(value = "档案编号", example = "ARC20230001")
    private String archiveFileNumber;

    @ApiModelProperty(value = "借阅人ID", example = "1")
    private Long borrowerId;

    @NotNull(message = "借阅人不能为空")
    @ApiModelProperty(value = "借阅人", required = true, example = "张三")
    private String borrower;

    @ApiModelProperty(value = "借阅部门", example = "财务部")
    private String borrowerDepartment;

    @ApiModelProperty(value = "借阅目的", example = "财务审计")
    private String purpose;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "借阅时间", example = "2023-03-15 14:30:00")
    private LocalDateTime borrowDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "计划归还时间", example = "2023-03-29 14:30:00")
    private LocalDateTime expectedReturnDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "实际归还时间", example = "2023-03-25 10:15:00")
    private LocalDateTime actualReturnDate;

    @ApiModelProperty(value = "状态", example = "BORROWED", notes = "可选值: BORROWED(已借出), RETURNED(已归还), OVERDUE(已逾期), LOST(已丢失), DAMAGED(已损坏), PENDING(待审批)")
    private String status;

    @ApiModelProperty(value = "状态描述", example = "已借出")
    private String statusDesc;

    @ApiModelProperty(value = "借阅天数", example = "14")
    private Integer days;

    @ApiModelProperty(value = "审批人", example = "李四")
    private String approver;

    @ApiModelProperty(value = "备注", example = "按时归还")
    private String remarks;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间", example = "2023-03-15 14:30:00")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "更新时间", example = "2023-03-25 10:15:00")
    private LocalDateTime updateTime;

    private String approvedBy; // 审批人

} 