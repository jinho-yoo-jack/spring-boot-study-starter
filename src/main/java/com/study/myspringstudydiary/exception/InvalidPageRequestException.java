package com.study.myspringstudydiary.exception;

public class InvalidPageRequestException extends RuntimeException {

    private final int requestedPage;
    private final int totalPages;

    public InvalidPageRequestException(int requestedPage, int totalPages) {
        super(String.format(
                "잘못된 페이지 요청입니다. 요청 페이지: %d, 전체 페이지: %d (0~%d)",
                requestedPage, totalPages, totalPages - 1
        ));
        this.requestedPage = requestedPage;
        this.totalPages = totalPages;
    }

    public int getRequestedPage() { return requestedPage; }
    public int getTotalPages() { return totalPages; }
}