package com.study.myspringstudydiary.dto.request;

public class PageRequest {

    private static final int MAX_SIZE = 100;

    private int page = 0;        // 페이지 번호 (0부터 시작)
    private int size = 10;       // 페이지당 항목 수
    private String sortBy = "createdAt";  // 정렬 기준
    private String sortDirection = "DESC"; // 정렬 방향

    // 기본 생성자
    public PageRequest() {
    }

    // Getter/Setter
    public int getPage() {
        return page < 0 ? 0 : page;
    }

    public int getSize() {
        return size <= 0 ? 10 : Math.min(size, MAX_SIZE); // 최대 100개 제한
    }

    public String getSortBy() {
        return sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    // 시작 인덱스 계산
    public int getOffset() {
        return page * size;
    }
}