package com.ray.archive.entity;

/**
 * 借阅状态枚举
 */
public enum BorrowStatus {
    BORROWED(1, "已借出"),
    RETURNED(2, "已归还"),
    OVERDUE(3, "已逾期"),
    LOST(4, "已丢失"),
    DAMAGED(5, "已损坏"),
    PENDING(0, "待审批");
    
    private final int value;
    private final String description;
    
    BorrowStatus(int value, String description) {
        this.value = value;
        this.description = description;
    }
    
    public int getValue() {
        return value;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * 根据值获取枚举
     */
    public static BorrowStatus fromValue(int value) {
        for (BorrowStatus status : BorrowStatus.values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        return null;
    }
} 