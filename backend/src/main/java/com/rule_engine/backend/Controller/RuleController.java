package com.rule_engine.backend.Controller;

import com.rule_engine.backend.DTO.RuleDTO;
import com.rule_engine.backend.DTO.RuleInfoResponse;
import com.rule_engine.backend.Model.CSVFile;
import com.rule_engine.backend.Model.Rule;
import com.rule_engine.backend.Service.CSVService;
import com.rule_engine.backend.Service.RuleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rule")
public class RuleController {
    private final RuleService ruleService;
    private final CSVService csvService;

    public RuleController(RuleService ruleService, CSVService csvService) {
        this.ruleService = ruleService;
        this.csvService = csvService;
    }

    @PostMapping("/create")
    public Rule createRule(@RequestBody RuleDTO ruleDTO) {
        System.out.println("at createRule: " + ruleDTO.getRuleString());
        return ruleService.createRule(ruleDTO.getRuleString());
    }

    @PostMapping("/apply/{ruleId}/{fileId}")
    public List<Map<String, Object>> applyRule(@PathVariable Long ruleId, @PathVariable Long fileId) {
        System.out.println("apply rule request received");
        return ruleService.applyRule(ruleId,fileId);
    }
    @GetMapping("/all")
    public List<RuleInfoResponse> getAllRules() {
        return ruleService.getAllRules();
    }
}

