package com.rule_engine.backend.Repository;

import com.rule_engine.backend.Model.Rule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RuleRepository extends JpaRepository<Rule, Long> {
    List<Rule> findAll();
}