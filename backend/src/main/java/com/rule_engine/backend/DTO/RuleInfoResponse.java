package com.rule_engine.backend.DTO;

public class RuleInfoResponse {
    private Long id;
    private String ruleString;

    public RuleInfoResponse(Long id, String ruleString) {
        this.id = id;
        this.ruleString = ruleString;
    }

    public Long getId() {
        return id;
    }

    public String getRuleString() {
        return ruleString;
    }
}

