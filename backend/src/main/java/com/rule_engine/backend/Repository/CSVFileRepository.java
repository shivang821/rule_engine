package com.rule_engine.backend.Repository;

import com.rule_engine.backend.Model.CSVFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CSVFileRepository extends JpaRepository<CSVFile, Long> {
}