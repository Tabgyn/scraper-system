package com.scraper.worker.exception;

public class ScrapeException extends RuntimeException {

    private final int httpStatus;

    public ScrapeException(String message) {
        super(message);
        this.httpStatus = 0;
    }

    public ScrapeException(String message, int httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public ScrapeException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = 0;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}