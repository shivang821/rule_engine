package com.rule_engine.backend.Model;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ruleString;  // Original rule as a string

    @Lob
    private String astJson;  // Serialized AST as JSON for storage



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRuleString() {
        return ruleString;
    }

    public void setRuleString(String ruleString) {
        this.ruleString = ruleString;
    }

    public String getAstJson() {
        return astJson;
    }

    public void setAstJson(String astJson) {
        this.astJson = astJson;
    }
}

