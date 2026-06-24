package com.rewards.dto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ErrorResponse {

    private String timestamp;
    private String path;
    private int code;
    private String message;
    private List<String> details;

    public ErrorResponse(int code, String message, String path, List<String> details) {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        this.code = code;
        this.message = message;
        this.path = path;
        this.details = details;
    }

    public String getTimestamp() { return timestamp; }
    public String getPath() { return path; }
    public int getCode() { return code; }
    public String getMessage() { return message; }
    public List<String> getDetails() { return details; }
}
