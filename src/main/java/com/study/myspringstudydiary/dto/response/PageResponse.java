package com.study.myspringstudydiary.dto.response;

import java.util.List;

public class PageResponse<T> {

    private List<T> content;      // 실제 데이터
    private int pageNumber;       // 현재 페이지 번호
    private int pageSize;         // 페이지 크기
    private long totalElements;   // 전체 데이터 개수
    private int totalPages;       // 전체 페이지 수
    private boolean first;        // 첫 페이지 여부
    private boolean last;         // 마지막 페이지 여부

    // 생성자
    public PageResponse(List<T> content, int pageNumber, int pageSize,
                        long totalElements) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.totalElements = totalElements;
        this.totalPages = (int) Math.ceil((double) totalElements / pageSize);
        this.first = pageNumber == 0;
        this.last = pageNumber >= totalPages - 1;
    }

    // 정적 팩토리 메서드
    public static <T> PageResponse<T> of(List<T> content, int pageNumber,
                                         int pageSize, long totalElements) {
        return new PageResponse<>(content, pageNumber, pageSize, totalElements);
    }

    // Getter 메서드들
    public List<T> getContent() {
        return content;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public int getPageSize() {
        return pageSize;
    }

    public long getTotalElements() {
        return totalElements;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isFirst() {
        return first;
    }

    public boolean isLast() {
        return last;
    }

    // 다음 페이지 존재 여부
    public boolean hasNext() {
        return !last;
    }

    // 이전 페이지 존재 여부
    public boolean hasPrevious() {
        return !first;
    }
}