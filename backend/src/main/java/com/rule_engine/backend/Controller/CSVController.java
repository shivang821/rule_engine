package com.rule_engine.backend.Controller;

import com.rule_engine.backend.DTO.FileInfoResponse;
import com.rule_engine.backend.Service.CSVService;
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

    public CSVController(CSVService csvService) {
        this.csvService = csvService;
    }

    @PostMapping("/upload")
    public Map<String, Object> uploadCSV(@RequestParam("file") MultipartFile file) throws IOException {
        System.out.println("upload Called");
        csvService.deleteAllFiles();
        return csvService.uploadCSV(file);
    }
    @DeleteMapping("/{fileId}")
    public ResponseEntity<String> deleteFile(@PathVariable Long fileId) {
        csvService.deleteFile(fileId);
        return ResponseEntity.ok("File deleted successfully");
    }
    @GetMapping("/all")
    public List<FileInfoResponse> getAllFiles() {
        return csvService.getAllFiles();
    }
}
