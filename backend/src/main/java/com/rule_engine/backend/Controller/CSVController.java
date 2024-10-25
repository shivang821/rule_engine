package com.rule_engine.backend.Controller;

import com.rule_engine.backend.DTO.FileInfoResponse;
import com.rule_engine.backend.Service.CSVService;
import com.rule_engine.backend.Service.RuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/csv")
public class CSVController {
    private final CSVService csvService;
    @Autowired
    RuleService ruleService;
    public CSVController(CSVService csvService) {
        this.csvService = csvService;
    }

    @PostMapping("/upload")
    public Map<String, Object> uploadCSV(@RequestParam("file") MultipartFile file) throws IOException {
        System.out.println("upload Called");
        csvService.deleteAllFiles();
        ruleService.deleteAllRules();
        return csvService.uploadCSV(file);
    }
    @DeleteMapping("/{fileId}")
    public ResponseEntity<String> deleteFile(@PathVariable Long fileId) {
        csvService.deleteFile(fileId);
        ruleService.deleteAllRules();
        return ResponseEntity.ok("File deleted successfully");
    }
    @GetMapping("/all")
    public List<FileInfoResponse> getAllFiles() {
        return csvService.getAllFiles();
    }
}
