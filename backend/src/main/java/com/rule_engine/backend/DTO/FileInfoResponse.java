package com.rule_engine.backend.DTO;

import java.util.List;

public class FileInfoResponse {
    private Long fileId;
    private String fileName;
    private List<String> columnNames; // New field to store column names

    public FileInfoResponse(Long fileId, String fileName, List<String> columnNames) {
        this.fileId = fileId;
        this.fileName = fileName;
        this.columnNames = columnNames;
    }

    // Getters and Setters
    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(List<String> columnNames) {
        this.columnNames = columnNames;
    }
}

