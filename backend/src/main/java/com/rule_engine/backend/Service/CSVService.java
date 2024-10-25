package com.rule_engine.backend.Service;
import com.rule_engine.backend.DTO.FileInfoResponse;
import com.rule_engine.backend.Model.CSVFile;
import com.rule_engine.backend.Repository.CSVFileRepository;
import org.apache.commons.csv.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CSVService {
    private final CSVFileRepository csvFileRepository;

    public CSVService(CSVFileRepository csvFileRepository) {
        this.csvFileRepository = csvFileRepository;
    }

    public Map<String, Object> uploadCSV(MultipartFile file) throws IOException {
        String content = new String(file.getBytes());
        List<String> extractedColumnNames = extractColumnNames(content);
        String columnNames = String.join(",", extractedColumnNames);
        CSVFile csvFile=new CSVFile(file.getOriginalFilename(),content,columnNames);
        csvFile = csvFileRepository.save(csvFile);

        Map<String, Object> response = new HashMap<>();
        response.put("fileId", csvFile.getId());
        response.put("columns", extractedColumnNames);
        return response;
    }
    public void deleteFile(Long fileId) {
        if (!csvFileRepository.existsById(fileId)) {
            throw new IllegalArgumentException("File with ID " + fileId + " not found");
        }
        csvFileRepository.deleteById(fileId);
    }
    public void deleteAllFiles() {
        csvFileRepository.deleteAll();  // Clear repository
    }
    public List<FileInfoResponse> getAllFiles() {
        return csvFileRepository.findAll().stream()
                .map(file -> {
                    List<String> columns = Arrays.asList(file.getColumnNames().split(",")); // Split the string back into a List
                    return new FileInfoResponse(file.getId(), file.getFileName(), columns); // Assuming FileInfoResponse has a constructor to accept columns
                })
                .collect(Collectors.toList());
    }
    private List<String> extractColumnNames(String content) throws IOException {
        try (Reader reader = new StringReader(content);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
            return new ArrayList<>(csvParser.getHeaderMap().keySet());
        }
    }
    public CSVFile getFileById(Long fileId) {
        return csvFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with ID: " + fileId));
    }
    public List<Map<String, Object>> getFileContent(Long fileId) {
        // Fetch the CSV file from the database
        CSVFile csvFile = csvFileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        // Retrieve the file content as a string
        String content = new String(csvFile.getContent().getBytes(), StandardCharsets.UTF_8); // Convert byte array to String

        // Parse the CSV content using the existing method
        return parseCSVContent(content);
    }

    // Existing method for parsing CSV content
    private List<Map<String, Object>> parseCSVContent(String content) {
        List<Map<String, Object>> rows = new ArrayList<>();

        try (Scanner scanner = new Scanner(content)) {
            String[] headers = scanner.nextLine().split(",");

            while (scanner.hasNextLine()) {
                String[] values = scanner.nextLine().split(",");
                Map<String, Object> row = new HashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    row.put(headers[i].trim(), parseValue(values[i].trim()));
                }
                rows.add(row);
            }
        }
        return rows;
    }

    // Assuming this method exists for parsing values
    private Object parseValue(String value) {
        // Implement your value parsing logic (e.g., parse to Integer, Double, etc.)
        if (value.matches("\\d+")) { // Example: If it's a digit, parse to Integer
            return Integer.parseInt(value);
        }
        return value; // Return as String by default
    }
}