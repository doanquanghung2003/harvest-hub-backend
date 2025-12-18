package com.example.harvesthubbackend.Utils;

import java.util.List;

public class PaginationUtils {
    
    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;
    
    /**
     * Parse page number from request parameter
     */
    public static int parsePage(String pageParam) {
        if (pageParam == null || pageParam.trim().isEmpty()) {
            return DEFAULT_PAGE;
        }
        try {
            int page = Integer.parseInt(pageParam);
            return Math.max(0, page);
        } catch (NumberFormatException e) {
            return DEFAULT_PAGE;
        }
    }
    
    /**
     * Parse page size from request parameter
     */
    public static int parseSize(String sizeParam) {
        if (sizeParam == null || sizeParam.trim().isEmpty()) {
            return DEFAULT_SIZE;
        }
        try {
            int size = Integer.parseInt(sizeParam);
            return Math.min(Math.max(1, size), MAX_SIZE);
        } catch (NumberFormatException e) {
            return DEFAULT_SIZE;
        }
    }
    
    /**
     * Create paginated response from list
     */
    public static <T> PageResponse<T> paginate(List<T> allItems, int page, int size) {
        int totalElements = allItems.size();
        int start = page * size;
        int end = Math.min(start + size, totalElements);
        
        List<T> content = start < totalElements 
            ? allItems.subList(start, end)
            : List.of();
        
        return new PageResponse<>(content, page, size, totalElements);
    }
}

