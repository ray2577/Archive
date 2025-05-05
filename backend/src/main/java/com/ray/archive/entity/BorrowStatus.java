package com.ray.archive.entity;

/**
 * 借阅状态枚举
 */
public enum BorrowStatus {
    BORROWED,  // 已借出
    RETURNED,  // 已归还
    OVERDUE,   // 已逾期
    LOST,      // 已丢失
    DAMAGED,   // 已损坏
    PENDING;   // 待审批
    
    /**
     * 根据值获取枚举
     */
    public static BorrowStatus fromValue(int value) {
        try {
            return BorrowStatus.values()[value];
        } catch (ArrayIndexOutOfBoundsException e) {
            return PENDING;
        }
    }
    
    /**
     * 从字符串获取枚举
     */
    public static BorrowStatus fromValue(String value) {
        if (value == null || value.isEmpty()) {
            return PENDING;
        }
        
        try {
            return BorrowStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return PENDING;
        }
    }
} 