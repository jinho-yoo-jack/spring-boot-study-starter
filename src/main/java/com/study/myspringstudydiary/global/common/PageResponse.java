package com.study.myspringstudydiary.global.common;

import java.util.List;

/**
 * Wrapper class for paginated responses
 *
 * @param <T> Type of content in the page
 */
public class PageResponse<T> {

    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    // Private constructor
    private PageResponse() {}

    /**
     * Create a page response
     */
    public static <T> PageResponse<T> of(
            List<T> content,
            int pageNumber,
            int pageSize,
            long totalElements) {

        PageResponse<T> response = new PageResponse<>();
        response.content = content;
        response.pageNumber = pageNumber;
        response.pageSize = pageSize;
        response.totalElements = totalElements;
        response.totalPages = (int) Math.ceil((double) totalElements / pageSize);
        response.first = pageNumber == 0;
        response.last = pageNumber == response.totalPages - 1 || response.totalPages == 0;

        return response;
    }

    // Getters
    public List<T> getContent() { return content; }
    public int getPageNumber() { return pageNumber; }
    public int getPageSize() { return pageSize; }
    public long getTotalElements() { return totalElements; }
    public int getTotalPages() { return totalPages; }
    public boolean isFirst() { return first; }
    public boolean isLast() { return last; }
}