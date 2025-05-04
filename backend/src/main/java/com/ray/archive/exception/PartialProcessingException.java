package com.ray.archive.exception;

import java.util.List;

/**
 * 部分处理异常，用于处理批量操作中的部分成功部分失败情况
 */
public class PartialProcessingException extends RuntimeException {
    
    private final List<String> errors;
    private final int processedCount;
    
    public PartialProcessingException(String message, List<String> errors, int processedCount) {
        super(message);
        this.errors = errors;
        this.processedCount = processedCount;
    }
    
    public List<String> getErrors() {
        return errors;
    }
    
    public int getProcessedCount() {
        return processedCount;
    }
} 