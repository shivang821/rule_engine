package com.rule_engine.backend.Model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class CSVFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    @Lob  // Store the CSV data as a large string
    private String content;
    private String columnNames;

    // Constructors

    public CSVFile() {
    }

    public CSVFile(String fileName, String content, String columnNames) {
        System.out.println("$$$$$$$ "+columnNames);
        this.fileName = fileName;
        this.content = content;
        this.columnNames = columnNames;
    }

    public String getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(String columnNames) {
        this.columnNames = columnNames;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


}