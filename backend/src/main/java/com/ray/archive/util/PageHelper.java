package com.ray.archive.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * 分页工具类
 */
public class PageHelper {
    
    /**
     * 创建分页对象
     * 
     * @param page 页码，从1开始
     * @param pageSize 每页条数
     * @param sortBy 排序字段
     * @param sortDirection 排序方向
     * @return Pageable分页对象
     */
    public static Pageable createPageable(int page, int pageSize, String sortBy, String sortDirection) {
        // 页码从0开始，需要减1
        int pageNumber = Math.max(0, page - 1);
        
        // 确保每页显示条数为正数
        int size = Math.max(1, pageSize);
        
        // 如果有排序字段
        if (sortBy != null && !sortBy.isEmpty()) {
            Sort.Direction direction = Sort.Direction.ASC;
            
            if (sortDirection != null && sortDirection.equalsIgnoreCase("desc")) {
                direction = Sort.Direction.DESC;
            }
            
            return PageRequest.of(pageNumber, size, Sort.by(direction, sortBy));
        } else {
            // 无排序
            return PageRequest.of(pageNumber, size);
        }
    }
    
    /**
     * 创建分页对象（带多字段排序）
     * 
     * @param page 页码，从1开始
     * @param pageSize 每页条数
     * @param sorts 排序信息，格式为：字段名,asc|desc
     * @return Pageable分页对象
     */
    public static Pageable createPageable(int page, int pageSize, String[] sorts) {
        // 页码从0开始，需要减1
        int pageNumber = Math.max(0, page - 1);
        
        // 确保每页显示条数为正数
        int size = Math.max(1, pageSize);
        
        // 如果有排序字段
        if (sorts != null && sorts.length > 0) {
            Sort sort = null;
            
            for (String sortExpression : sorts) {
                String[] parts = sortExpression.split(",");
                if (parts.length > 0) {
                    String fieldName = parts[0];
                    Sort.Direction direction = Sort.Direction.ASC;
                    
                    if (parts.length > 1 && parts[1].equalsIgnoreCase("desc")) {
                        direction = Sort.Direction.DESC;
                    }
                    
                    if (sort == null) {
                        sort = Sort.by(direction, fieldName);
                    } else {
                        sort = sort.and(Sort.by(direction, fieldName));
                    }
                }
            }
            
            return sort != null ? PageRequest.of(pageNumber, size, sort) : PageRequest.of(pageNumber, size);
        } else {
            // 无排序
            return PageRequest.of(pageNumber, size);
        }
    }
} 